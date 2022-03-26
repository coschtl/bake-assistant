package at.coschtl.bakeassistant.ui.preparation;

import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    private static final String INSTANCE_STATE_CALCULATOR = "InstructionCalculator";
    private static final String INSTANCE_STATE_POSITION = "currentInstructionPosition";
    private static final String TAG_BAKE_ASSISTANT = BakeAssistant.class.getName();

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
    private InstructionFinishedReceiver instructionFinishedReceiver;
    private boolean closeOnBack;
    private boolean forceCloseOnBack;
    private long lastBackPressTime;
    private View startButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        closeOnBack = false;
        forceCloseOnBack = false;
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
            Toast.makeText(this, R.string.instructions_scheduled, Toast.LENGTH_LONG).show();
            startNextAlarm();
            startButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BakeAssistant.CONTEXT.unregisterReceiver(instructionFinishedReceiver);
        cancelAlarm();
    }

    private void cancelAlarm() {
        WorkManager.getInstance(this).cancelAllWorkByTag(TAG_BAKE_ASSISTANT);
    }

    public void showTimeSelectionUi() {
        findViewById(R.id.selectTime).setVisibility(View.VISIBLE);
        findViewById(R.id.preparation).setVisibility(View.GONE);
        timeSelectorVisible = true;
    }

    public void hideTimeSelectionUi() {
        findViewById(R.id.selectTime).setVisibility(View.GONE);
        findViewById(R.id.preparation).setVisibility(View.VISIBLE);
        timeSelectorVisible = false;
    }

    @Override
    public void onBackPressed() {
        if (timeSelectorVisible) {
            hideTimeSelectionUi();
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        System.out.println("onSaveInstanceState");
        outState.putSerializable(BakeAssistant.PKG_PREF + INSTANCE_STATE_CALCULATOR, instructionsAdapter.getInstructionCalculator());
        outState.putInt(BakeAssistant.PKG_PREF + INSTANCE_STATE_POSITION, currentInstructionPosition);
    }

    public void startNextAlarm() {
        //handle last instruction
        long addMillis = 0;
        if (currentInstructionPosition > 0) {
            Instruction lastInstruction = instructionsAdapter.getItem(currentInstructionPosition - 1);
            lastInstruction.setActive(false);
            lastInstruction.setDone(true);
        } else {
            addMillis = 3000;
        }
        addMillis = 10000;
        Instruction currentInstruction = instructionsAdapter.getItem(currentInstructionPosition);
        currentInstruction.setActive(true);
        if (currentInstructionPosition >= instructionsAdapter.getCount() - 1) {
            instructionsAdapter.notifyDataSetChanged();
            forceCloseOnBack = true;
            cancelAlarm();
            return;
        }
        Instruction nextInstruction = instructionsAdapter.getItem(++currentInstructionPosition);
        instructionsAdapter.notifyDataSetChanged();

        System.out.println("startNextAlarm: " + currentInstructionPosition);

        Data inputData = new Data.Builder()
                .putBoolean(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_HAS_ALARM, nextInstruction.hasAlarm())
                .putString(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_ACTION, nextInstruction.getAction())
                .putString(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_TIMESPAN_STRING, nextInstruction.getTimespanString())
                .build();
        long delay = nextInstruction.getTimeMin().date().getTime() - System.currentTimeMillis();
        if (delay < 0) {
            delay = 0;
        }
        WorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(TAG_BAKE_ASSISTANT)
                .build();
        WorkManager.getInstance(this).enqueue(uploadWorkRequest);

        System.out.println("alarm scheduled");

    }
}
