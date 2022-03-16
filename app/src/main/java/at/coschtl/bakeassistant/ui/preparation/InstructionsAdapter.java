package at.coschtl.bakeassistant.ui.preparation;

import static at.coschtl.bakeassistant.util.UiUtil.setText;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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

    public Instruction getActiveInstruction() {
        for (int i=1; i<getCount()-1; i++) {
            Instruction instruction = getItem(i);
            if (!instruction.isDone()) {
                return instruction;
            }
        }
        return null;
    }

    void updateRow(LinearLayout row, Instruction instruction) {
        setText(R.id.time_min, instruction.getTimeMin().toString(), row);
        if (instruction.hasTimespan()) {
            row.findViewById(R.id.spacer).setVisibility(View.VISIBLE);
            setText(R.id.time_max, instruction.getTimeMax().toString(), row);
        } else {
            row.findViewById(R.id.spacer).setVisibility(View.INVISIBLE);
            setText(R.id.time_max, "", row);
        }
        setText(R.id.time_max, instruction.hasTimespan() ? instruction.getTimeMax().toString() : "", row);
        setText(R.id.action, instruction.getAction(), row);
        CheckBox interaction = row.findViewById(R.id.done);
        interaction.setChecked(instruction.isDone());
        interaction.setClickable(!instruction.isDone());
        interaction.setVisibility(instruction.showInteraction() ? View.VISIBLE : View.INVISIBLE);
        interaction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                instruction.setDone(isChecked);
                buttonView.setClickable(!isChecked);
            }
        });
    }

    public void notifyInstructionChanged(int position) {
        instructionCalculator.recalculate(position);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LinearLayout row = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.instruction_row, null);
        Instruction instruction = getItem(position);
        updateRow(row, instruction);
        row.setOnLongClickListener(v -> {
            new TimeSetter((PrepareRecipe) getContext(), this, row, position).show();
            return false;
        });
        return row;
    }

}
