package at.coschtl.bakeassistant.ui.recipe;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
import at.coschtl.bakeassistant.model.Action;
import at.coschtl.bakeassistant.model.DurationUnit;
import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.model.Step;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;
import at.coschtl.bakeassistant.ui.preparation.PrepareRecipe;

public class EditRecipe extends AppCompatActivity implements View.OnClickListener {

    private static final Map<DurationUnit, Integer> DURATION_TO_POS;

    static {
        DURATION_TO_POS = new LinkedHashMap<>();
        DURATION_TO_POS.put(DurationUnit.MINUTES, 0);
        DURATION_TO_POS.put(DurationUnit.HOURS, 1);
    }

    private RecipeDataAdapter dataAdapter;
    private RecyclerView stepsListView;
    private TextView noStepsTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_recipe);
        Bundle extras = getIntent().getExtras();
        long recipeId = extras.getLong(BakeAssistant.EXTRA_RECIPE_ID);
        final EditText title = findViewById(R.id.recipe);
        dataAdapter = new RecipeDataAdapter(recipeId);
        if (recipeId >= 0) {
            title.setText(dataAdapter.getRecipe().getName());
            title.setEnabled(false);
            title.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    title.setEnabled(true);
                    return true;
                }
            });
        } else {
            title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // not needed
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // not needed
                }

                @Override
                public void afterTextChanged(Editable s) {
                    dataAdapter.getRecipe().setName(title.getText().toString());
                }
            });
        }

        noStepsTextView = findViewById(R.id.steps_no_recipes);
        stepsListView = findViewById(R.id.steps_listview);
        RecipeStepsAdapter adapter = new RecipeStepsAdapter( recipe().getSteps(), this::showStepPopup);
        stepsListView.setAdapter(adapter);
        stepsListView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
               return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int adapterPosition =  viewHolder.getAdapterPosition();
                adapter.deleteStep(adapterPosition);
                recipe().getSteps().remove(adapterPosition);
                System.out.println("SWIPED: " + (direction == ItemTouchHelper.LEFT ? "left" : (direction == ItemTouchHelper.RIGHT ? "right" : "unknown: "+direction)));
            }
        });
        itemTouchHelper.attachToRecyclerView(stepsListView);

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                if (recipeId >= 0) {
                    dataAdapter.loadRecipe(recipeId);
                    updateUi();
                }
            }
        });

        findViewById(R.id.add_step_button).setOnClickListener(this);
        findViewById(R.id.start_now_button).setOnClickListener(this);
        updateUi();
    }

    private void updateUi() {
        if (recipe().getSteps().isEmpty()) {
            noStepsTextView.setVisibility(View.VISIBLE);
            stepsListView.setVisibility(View.GONE);
        } else {
            noStepsTextView.setVisibility(View.GONE);
            stepsListView.setVisibility(View.VISIBLE);
            ((RecipeStepsAdapter) stepsListView.getAdapter()).setData(recipe().getSteps());
        }
        stepsListView.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            dataAdapter.saveRecipe();
            updateUi();
        }
        dataAdapter.finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_step_button) {
            showStepPopup(BakeAssistant.CONTEXT.getResources().getString(R.string.new_step), null);
        } else if (v.getId() == R.id.start_now_button) {
            Intent intent = new Intent(EditRecipe.this, PrepareRecipe.class);
            intent.putExtra(BakeAssistant.EXTRA_RECIPE_ID, recipe().getId());
            startActivityForResult(intent, 1);
            System.out.println(new InstructionCalculator(recipe()).calculateFromEnd().getInstructions());
            System.out.println(new InstructionCalculator(recipe()).calculateFromStart().getInstructions());
        }
    }

    public void showStepPopup(String title, Step step) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.edit_step, (ViewGroup) findViewById(android.R.id.content).getRootView(), false);

        Spinner timeUnit = viewInflated.findViewById(R.id.time_unit);
        ArrayAdapter<DurationUnit> adapter = new ArrayAdapter<DurationUnit>(this,
                android.R.layout.simple_spinner_item, new DurationUnit[]{DurationUnit.MINUTES, DurationUnit.HOURS});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeUnit.setAdapter(adapter);

        final AutoCompleteTextView action = viewInflated.findViewById(R.id.action);
        List<Action> allActions = dataAdapter.loadActions();
        ArrayAdapter<Action> actionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allActions);
        action.setAdapter(actionsAdapter);


        final EditText durationMin = viewInflated.findViewById(R.id.durationMin);
        final EditText durationMax = viewInflated.findViewById(R.id.durationMax);
        builder.setView(viewInflated);

        if (step != null) {
            action.setText(step.getAction().getName());
            durationMin.setText(Integer.toString(step.getDurationMin()));
            durationMax.setText(Integer.toString(step.getDurationMax()));
            timeUnit.setSelection(adapter.getPosition(step.getDurationUnit()));
        }

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Step stepToSave;
                    if (step == null) {
                        stepToSave = recipe().addNewStep();
                    } else {
                        stepToSave = step;
                    }
                    stepToSave.setAction(dataAdapter.getOrCreateAction(action.getText().toString()));
                    int durationMinTime = Integer.parseInt(durationMin.getText().toString());
                    stepToSave.setDurationMin(durationMinTime);
                    int durationMaxTime;
                    try {
                        durationMaxTime = Integer.parseInt(durationMax.getText().toString());
                    } catch (Exception e) {
                        durationMaxTime = durationMinTime;
                    }
                    stepToSave.setDurationMax(durationMaxTime);
                    stepToSave.setDurationUnit(DurationUnit.byLabel(timeUnit.getSelectedItem().toString()));
                    dialog.dismiss();
                    updateUi();
                } catch (Exception e) {
                    // do not close dialog
                    System.out.println("no int");
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        RecipeStepsAdapter adapter = (RecipeStepsAdapter) stepsListView.getAdapter();
        Step step = adapter.getAktLongClickPosition().getItem();
        int currentPos = getStepPosition(step);
        switch (item.getItemId()) {
            case RecipeStepsAdapter.ViewHolder.MENU_EDIT:
                String title = MessageFormat.format(getString(R.string.title_edit_step), currentPos+1);
                showStepPopup(title,step);
                break;
            case RecipeStepsAdapter.ViewHolder.MENU_UP:
                if (currentPos > 0) {
                    move(currentPos, currentPos-1);
                }
                break;
            case RecipeStepsAdapter.ViewHolder.MENU_DOWN:
                if (currentPos < adapter.getItemCount()-1) {
                    move(currentPos, currentPos+1);
                }
                break;
        }
        System.out.println("action: " + item.getItemId() + " - pos: " +  ((RecipeStepsAdapter) stepsListView.getAdapter()).getAktLongClickPosition());
        return super.onContextItemSelected(item);
    }

    private int getStepPosition(Step step) {
        List<Step> steps = recipe().getSteps();
        for (int i=0; i<steps.size(); i++) {
            if (steps.get(i).equals(step)) {
                return i;
            }
        }
        return -1;
    }

    private void move(int prevPos, int newPos) {
        ((RecipeStepsAdapter) stepsListView.getAdapter()).moveStep(prevPos, newPos);
        List<Step> steps = recipe().getSteps();
        steps.add(newPos, steps.remove(prevPos));
    }

    private Recipe recipe() {
        return dataAdapter.getRecipe();
    }
}
