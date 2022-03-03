package at.coschtl.bakeassistant.ui.preparation;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.coschtl.bakeassistant.InstructionCalculator;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.db.RecipeDbAdapter;
import at.coschtl.bakeassistant.model.Action;
import at.coschtl.bakeassistant.model.DurationUnit;
import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.model.Step;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;
import at.coschtl.bakeassistant.ui.recipe.RecipeStepsAdapter;

public class PrepareRecipe extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final Map<DurationUnit, Integer> DURATION_TO_POS;

    static {
        DURATION_TO_POS = new LinkedHashMap<>();
        DURATION_TO_POS.put(DurationUnit.MINUTES, 0);
        DURATION_TO_POS.put(DurationUnit.HOURS, 1);
    }

    private InstructionsAdapter instructionsAdapter;
    private ListView instructionsListView;
    private View selectTime;


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
        instructionsAdapter = new InstructionsAdapter(this, -1, calculator, this);
        instructionsListView.setAdapter(instructionsAdapter);
        ((TextView)findViewById(R.id.recipe)).setText(recipe.getName() + ":");
        selectTime = findViewById(R.id.selectTime);

        TextView startTime = findViewById(R.id.startTime);
        TimeSetter startTimeSetter = new TimeSetter(selectTime, new SettableTime.TextViewSettableTime(startTime), findViewById(R.id.preparation));
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimeSetter.show();
            }
        });
    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.add_step_button) {
//            showStepPopup(BakeAssistant.CONTEXT.getResources().getString(R.string.new_step), null);
//        } else if (v.getId() == R.id.start_now_button) {
//            System.out.println(new InstructionCalculator(recipe()).calculateInstructions(new Date()));
//        } else if (v.getId() == R.id.start_later_button) {
//            System.out.println(new InstructionCalculator(recipe()).calculateInstructions(new Date()));
//        }
    }

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(this, "long click", Toast.LENGTH_SHORT).show();
        return false;
    }


//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        RecipeStepsAdapter adapter = (RecipeStepsAdapter) stepsListView.getAdapter();
//        Step step = adapter.getAktLongClickPosition().getStep();
//        int currentPos = getStepPosition(step);
//        switch (item.getItemId()) {
//            case RecipeStepsAdapter.ViewHolder.MENU_EDIT:
//                String title = MessageFormat.format(getString(R.string.title_edit_step), currentPos + 1);
//                showStepPopup(title, step);
//                break;
//            case RecipeStepsAdapter.ViewHolder.MENU_UP:
//                if (currentPos > 0) {
//                    move(currentPos, currentPos - 1);
//                }
//                break;
//            case RecipeStepsAdapter.ViewHolder.MENU_DOWN:
//                if (currentPos < adapter.getItemCount() - 1) {
//                    move(currentPos, currentPos + 1);
//                }
//                break;
//        }
//        System.out.println("action: " + item.getItemId() + " - pos: " + ((RecipeStepsAdapter) stepsListView.getAdapter()).getAktLongClickPosition());
//        return super.onContextItemSelected(item);
//    }

}
