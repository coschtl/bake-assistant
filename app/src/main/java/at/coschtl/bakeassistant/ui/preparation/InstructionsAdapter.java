package at.coschtl.bakeassistant.ui.preparation;

import static at.coschtl.bakeassistant.util.UiUtil.setText;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import at.coschtl.bakeassistant.Instruction;
import at.coschtl.bakeassistant.InstructionCalculator;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.model.Recipe;

public class InstructionsAdapter extends ArrayAdapter<Instruction> {

    private final InstructionCalculator instructionCalculator;

    public InstructionsAdapter(@NonNull Activity activity, int resource, @NonNull InstructionCalculator instructionCalculator) {
        super(activity, resource, instructionCalculator.getInstructions());
        this.instructionCalculator = instructionCalculator;
    }

    public Recipe getRecipe() {
        return instructionCalculator.getRecipe();
    }

    public InstructionCalculator getInstructionCalculator() {
        return instructionCalculator;
    }

    public Instruction getActiveInstruction() {
        for (int i = 1; i < getCount() - 1; i++) {
            Instruction instruction = getItem(i);
            if (!instruction.isDone()) {
                return instruction;
            }
        }
        return null;
    }

    void updateRow(View row, Instruction instruction) {
        setText(R.id.time_min, instruction.getTimeMin().toString(), row);
        if (instruction.hasTimespan()) {
            row.findViewById(R.id.spacer).setVisibility(View.VISIBLE);
            setText(R.id.time_max, instruction.getTimeMax().toString(), row);
        } else {
            row.findViewById(R.id.spacer).setVisibility(View.INVISIBLE);
            setText(R.id.time_max, "", row);
        }
        row.findViewById(R.id.alarm).setVisibility(instruction.hasAlarm() ? View.VISIBLE : View.INVISIBLE);
        setText(R.id.time_max, instruction.hasTimespan() ? instruction.getTimeMax().toString() : "", row);
        TextView actionView = setText(R.id.action, instruction.getAction(), row);
        if (instruction.isActive()) {
            actionView.setTextColor(Color.RED);
        }
        CheckBox interaction = row.findViewById(R.id.done);
        interaction.setChecked(instruction.isDone());
        interaction.setVisibility(instruction.showInteraction() ? View.VISIBLE : View.INVISIBLE);
        interaction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                instruction.setDone(isChecked);
            }
        });
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = LayoutInflater.from(getContext()).inflate(R.layout.instruction_row, null);
        Instruction instruction = getItem(position);
        updateRow(row, instruction);
        row.setOnLongClickListener(v -> {
            row.getRootView().findViewById(R.id.start_now_button).setVisibility(View.GONE);
            PrepareRecipe prepareRecipe = (PrepareRecipe) getContext();
            if (!prepareRecipe.isPreparationRunning() || position >= prepareRecipe.getCurrentInstructionPosition()) {
                new TimeSetter(prepareRecipe, this, row, position).show();
            }
            return false;
        });
        return row;
    }

}
