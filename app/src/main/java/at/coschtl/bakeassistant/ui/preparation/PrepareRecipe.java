package at.coschtl.bakeassistant.ui.preparation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import at.coschtl.bakeassistant.Instruction;
import at.coschtl.bakeassistant.InstructionCalculator;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.db.RecipeDbAdapter;
import at.coschtl.bakeassistant.model.DurationUnit;
import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.ui.InstructionNotification;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;
import at.coschtl.bakeassistant.util.SerializationUtil;

public class PrepareRecipe extends AppCompatActivity implements AlarmStarter, View.OnClickListener {

    private static final String INSTANCE_STATE_CALCULATOR = "InstructionCalculator";
    private static final String INSTANCE_STATE_POSITION = "currentInstructionPosition";

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
    private int backCount;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;
    private  View startButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backCount = 0;
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
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
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
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
            if (backCount++ == 0) {
                Toast.makeText(this, R.string.back_will_abort, Toast.LENGTH_LONG).show();
            } else {
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent = null;
                }
                super.onBackPressed();
            }
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
        Instruction currentInstruction = instructionsAdapter.getItem(currentInstructionPosition);
        currentInstruction.setActive(true);
        if (currentInstructionPosition >= instructionsAdapter.getCount() - 1) {
            return;
        }
        Instruction nextInstruction = instructionsAdapter.getItem(++currentInstructionPosition);
        instructionsAdapter.notifyDataSetChanged();

        System.out.println("startNextAlarm: " + currentInstructionPosition);

        Intent intent = new Intent(this, InstructionNotification.class);

        System.out.println("current step is: " + currentInstruction.getAction() + ", next step is: " + nextInstruction.getAction());
        intent.putExtra(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_INSTRUCTION, SerializationUtil.serialize(nextInstruction));

        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        long alarmTime = nextInstruction.getTimeMin().date().getTime() + addMillis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            System.out.println("scheduling alarm WhileIdle for " + new Date(alarmTime));
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        } else {
            System.out.println("scheduling alarm for " + new Date(alarmTime));
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        }
        System.out.println("alarm scheduled");

    }

}
