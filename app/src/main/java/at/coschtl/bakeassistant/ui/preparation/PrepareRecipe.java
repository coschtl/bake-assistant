package at.coschtl.bakeassistant.ui.preparation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
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

public class PrepareRecipe extends AppCompatActivity implements AlarmStarter {

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
    private   InstructionFinishedReceiver instructionFinishedReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preparation);
        Bundle extras = getIntent().getExtras();
        long recipeId = extras.getLong(BakeAssistant.EXTRA_RECIPE_ID);

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

        Toast.makeText(this, R.string.instructions_scheduled, Toast.LENGTH_LONG).show();
        startNextAlarm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BakeAssistant.CONTEXT.unregisterReceiver(instructionFinishedReceiver);
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
            super.onBackPressed();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        InstructionCalculator calculator = (InstructionCalculator) savedInstanceState.get(INSTANCE_STATE_CALCULATOR);
        if (calculator != null) {
            instructionsAdapter = new InstructionsAdapter(this, -1, calculator);
            instructionsListView.setAdapter(instructionsAdapter);
            currentInstructionPosition = savedInstanceState.getInt(INSTANCE_STATE_POSITION);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        System.out.println("onSaveInstanceState");
        outState.putSerializable(INSTANCE_STATE_CALCULATOR, instructionsAdapter.getInstructionCalculator());
        outState.putInt(INSTANCE_STATE_POSITION, currentInstructionPosition);
    }

    public void startNextAlarm() {
        instructionsAdapter.getItem(currentInstructionPosition).setDone(true);
        instructionsAdapter.notifyDataSetChanged();
        currentInstructionPosition++;
        if (currentInstructionPosition >= instructionsAdapter.getCount()-1) {
            return;
        }

        System.out.println("startNextAlarm: " + currentInstructionPosition);

        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, InstructionNotification.class);
        Instruction instruction = instructionsAdapter.getItem(currentInstructionPosition);
        System.out.println("next step is: " + instruction.getAction());
        intent.putExtra(InstructionNotification.EXTRA_INSTRUCTION, instruction);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

//        Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.SECOND, 5);
//        alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, instruction.getTimeMin().date().getTime(), pendingIntent);

        System.out.println("alarm scheduled");

    }

}
