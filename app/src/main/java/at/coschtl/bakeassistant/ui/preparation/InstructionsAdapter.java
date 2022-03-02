package at.coschtl.bakeassistant.ui.preparation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

import at.coschtl.bakeassistant.Instruction;
import at.coschtl.bakeassistant.InstructionCalculator;
import at.coschtl.bakeassistant.R;

public class InstructionsAdapter extends ArrayAdapter<Instruction> {

    private final InstructionCalculator instructionCalculator;
    private final View.OnLongClickListener onLongClickListener;

    public InstructionsAdapter(@NonNull Context context, int resource, @NonNull InstructionCalculator instructionCalculator, View.OnLongClickListener onLongClickListener) {
        super(context, resource, instructionCalculator.calculateInstructions(new Date()));
        this.instructionCalculator = instructionCalculator;
        this.onLongClickListener=onLongClickListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LinearLayout row;
        if (convertView instanceof LinearLayout) {
            row = (LinearLayout) convertView;
        } else {
            row = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.instruction_row, null);
        }
        Instruction instruction = getItem(position);
        setText(R.id.time_min, instruction.getFormatedTimeMin(), row);
        setText(R.id.time_max, instruction.hasTimespan() ? instruction.getFormatedTimeMax() : "", row);
        setText(R.id.action, instruction.getAction(), row);
        ((CheckBox) row.findViewById(R.id.done)).setChecked(instruction.isDone());
        row.setOnLongClickListener(onLongClickListener);
        return row;
    }

    private void setText(int id, String text, View parent) {
        ((TextView) parent.findViewById(id)).setText(text);
    }

}
