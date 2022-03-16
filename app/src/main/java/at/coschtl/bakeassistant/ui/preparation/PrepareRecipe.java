package at.coschtl.bakeassistant.ui.preparation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import at.coschtl.bakeassistant.InstructionCalculator;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.db.RecipeDbAdapter;
import at.coschtl.bakeassistant.model.DurationUnit;
import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.ui.InstructionNotification;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;

public class PrepareRecipe extends AppCompatActivity implements View.OnClickListener {

    private static final Map<DurationUnit, Integer> DURATION_TO_POS;

    static {
        DURATION_TO_POS = new LinkedHashMap<>();
        DURATION_TO_POS.put(DurationUnit.MINUTES, 0);
        DURATION_TO_POS.put(DurationUnit.HOURS, 1);
    }

    private InstructionsAdapter instructionsAdapter;
    private ListView instructionsListView;
    private boolean timeSelectorVisible;

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
        findViewById(R.id.start_now_button).setOnClickListener(this);
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
    public void onClick(View v) {
        if (v.getId() == R.id.start_now_button) {
            AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, InstructionNotification.class);
            intent.putExtra(BakeAssistant.EXTRA_RECIPE_ID, instructionsAdapter.getRecipe().getId());
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 5);
            alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);

            System.out.println("STARTED");
        }
    }

}
