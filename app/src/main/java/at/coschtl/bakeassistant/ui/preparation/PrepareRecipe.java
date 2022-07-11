package at.coschtl.bakeassistant.ui.preparation;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import at.coschtl.bakeassistant.Instruction;
import at.coschtl.bakeassistant.InstructionCalculator;
import at.coschtl.bakeassistant.NotificationWorker;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.db.RecipeDbAdapter;
import at.coschtl.bakeassistant.model.DurationUnit;
import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.ui.InstructionNotification;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;


public class PrepareRecipe extends AppCompatActivity implements AlarmStarter, View.OnClickListener {

    public static final String EXTRA_ADJUST_TIME_SECONDS = "adjustTimeSecondss";
    private static final String INSTANCE_STATE_CALCULATOR = "InstructionCalculator";
    private static final String INSTANCE_STATE_POSITION = "currentInstructionPosition";
    private static final Logger LOGGER = Logger.getLogger(PrepareRecipe.class.getName());

    private static final Map<DurationUnit, Integer> DURATION_TO_POS;

    static {
        DURATION_TO_POS = new LinkedHashMap<>();
        DURATION_TO_POS.put(DurationUnit.MINUTES, 0);
        DURATION_TO_POS.put(DurationUnit.HOURS, 1);
    }

    private InstructionsAdapter instructionsAdapter;
    private ListView instructionsListView;
    private boolean timeSelectorVisible;
    private int currentInstructionPosition;
    private boolean preparationRunning;
    private InstructionFinishedReceiver instructionFinishedReceiver;
    private boolean closeOnBack;
    private boolean forceCloseOnBack;
    private long lastBackPressTime;
    private View startButton;
    private boolean startInTheMiddle;
    private int minimumStepSet = -1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        closeOnBack = false;
        forceCloseOnBack = true;
        setContentView(R.layout.preparation);
        Bundle extras = getIntent().getExtras();
        long recipeId = extras.getLong(BakeAssistant.PKG_PREF + BakeAssistant.EXTRA_RECIPE_ID);

        instructionsListView = findViewById(R.id.instructions_listview);
        RecipeDbAdapter recipeDbAdapter = new RecipeDbAdapter();
        Recipe recipe = recipeDbAdapter.getRecipe(recipeId);
        InstructionCalculator calculator = new InstructionCalculator(recipe);
        recipeDbAdapter.close();
        instructionsAdapter = new InstructionsAdapter(this, -1, calculator);
        instructionsListView.setAdapter(instructionsAdapter);
        ((TextView) findViewById(R.id.recipe)).setText(recipe.getName() + ":");

        instructionFinishedReceiver = new InstructionFinishedReceiver(this);
        BakeAssistant.CONTEXT.registerReceiver(instructionFinishedReceiver, new IntentFilter(InstructionNotification.class.getName()));

        startButton = findViewById(R.id.start_now_button);
        startButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_now_button) {
            startInTheMiddle = false;
            Toast.makeText(this, R.string.instructions_scheduled, Toast.LENGTH_LONG).show();
            forceCloseOnBack = false;
            startButton.setVisibility(View.GONE);
            long origStartTime = instructionsAdapter.getItem(0).getTimeMin().date().getTime();
            long now = System.currentTimeMillis();
            if (origStartTime < now && origStartTime + 90000L > now) {
                instructionsAdapter.getInstructionCalculator().recalculateBySeconds(0, (int) ((now - origStartTime + 1000L) / 1000L), 0);
                instructionsAdapter.notifyDataSetChanged();
            }
            startNextAlarm(0);
        }
    }

    public boolean isPreparationRunning() {
        return preparationRunning;
    }

    public int getMinimumStepSet() {
        for (int i = instructionsAdapter.getCount() - 1; i < minimumStepSet; i--) {
            if (instructionsAdapter.getItem(i).isDone()) {
                return i;
            }
        }
        return minimumStepSet;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BakeAssistant.CONTEXT.unregisterReceiver(instructionFinishedReceiver);
        cancelAlarm();
    }

    private void cancelAlarm() {
        WorkManager.getInstance(this).cancelAllWorkByTag(BakeAssistant.TAG_BAKE_ASSISTANT);
    }

    public void showTimeSelectionUi() {
        findViewById(R.id.selectTime).setVisibility(View.VISIBLE);
        findViewById(R.id.preparation).setVisibility(View.GONE);
        timeSelectorVisible = true;
    }

    public void hideTimeSelectionUi(int selectedStep) {
        findViewById(R.id.selectTime).setVisibility(View.GONE);
        findViewById(R.id.preparation).setVisibility(View.VISIBLE);
        timeSelectorVisible = false;
        if (isPreparationRunning() && instructionsAdapter.getItem(currentInstructionPosition).getTimeMin().date().before(new Date())) {
            cancelAlarm();
            startInTheMiddle = true;
            startNextAlarm(0);
        }
        if (selectedStep >= 0) {
            if (minimumStepSet >= 0) {
                minimumStepSet = Math.min(minimumStepSet, selectedStep);
            } else {
                minimumStepSet = selectedStep;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (timeSelectorVisible) {
            hideTimeSelectionUi(-1);
        } else {
            if (lastBackPressTime + 10000 < System.currentTimeMillis()) {
                closeOnBack = false;
            }
            if (closeOnBack || forceCloseOnBack) {
                cancelAlarm();
                super.onBackPressed();
            } else {
                Toast.makeText(this, R.string.back_will_abort, Toast.LENGTH_LONG).show();
                closeOnBack = true;
            }
            lastBackPressTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        InstructionCalculator calculator = (InstructionCalculator) savedInstanceState.get(BakeAssistant.PKG_PREF + INSTANCE_STATE_CALCULATOR);
        if (calculator != null) {
            instructionsAdapter = new InstructionsAdapter(this, -1, calculator);
            instructionsListView.setAdapter(instructionsAdapter);
            currentInstructionPosition = savedInstanceState.getInt(BakeAssistant.PKG_PREF + INSTANCE_STATE_POSITION);
        }
    }

    public int getCurrentInstructionPosition() {
        return currentInstructionPosition;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BakeAssistant.PKG_PREF + INSTANCE_STATE_CALCULATOR, instructionsAdapter.getInstructionCalculator());
        outState.putInt(BakeAssistant.PKG_PREF + INSTANCE_STATE_POSITION, currentInstructionPosition);
    }

    public void startNextAlarm(int adjustTimeSeconds) {
        //handle last instruction
        if (currentInstructionPosition > 0) {
            Instruction lastInstruction = instructionsAdapter.getItem(currentInstructionPosition - 1);
            lastInstruction.setActive(false);
            lastInstruction.setDone(true);
        }
        Instruction currentInstruction = instructionsAdapter.getItem(currentInstructionPosition);
        currentInstruction.setActive(true);
        if (currentInstructionPosition >= instructionsAdapter.getCount() - 1) {
            instructionsAdapter.notifyDataSetChanged();
            forceCloseOnBack = true;
            cancelAlarm();
            return;
        }
        if (adjustTimeSeconds > 0) {
            instructionsAdapter.getInstructionCalculator().recalculateRemaining(currentInstructionPosition, adjustTimeSeconds);
            instructionsAdapter.notifyDataSetChanged();
        }
        Instruction nextInstruction = instructionsAdapter.getItem(++currentInstructionPosition);
        instructionsAdapter.notifyDataSetChanged();

        long delay = nextInstruction.getTimeMin().date().getTime() - System.currentTimeMillis();
        if (delay < 1000 * adjustTimeSeconds && !isPreparationRunning()) {
            if (startInTheMiddle) {
                startNextAlarm(adjustTimeSeconds);
                return;
            } else {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                startInTheMiddle = true;
                                startNextAlarm(adjustTimeSeconds);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                startButton.setVisibility(View.VISIBLE);
                                forceCloseOnBack = true;
                                currentInstruction.setActive(false);
                                instructionsAdapter.notifyDataSetChanged();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.start_in_the_middle)
                        .setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener)
                        .show();
            }
            return;
        }
        preparationRunning = true;
        Data inputData = new Data.Builder()
                .putBoolean(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_HAS_ALARM, nextInstruction.hasAlarm())
                .putBoolean(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_SUPPRESS_ADJUST_ROW, nextInstruction.getType() == Instruction.Type.LAST)
                .putString(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_ACTION, nextInstruction.getAction())
                .putString(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_TIMESPAN_STRING, nextInstruction.getTimespanString())
                .build();
        WorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(BakeAssistant.TAG_BAKE_ASSISTANT)
                .build();
        WorkManager.getInstance(this).enqueue(uploadWorkRequest);

        LOGGER.fine("alarm scheduled");
    }
}
