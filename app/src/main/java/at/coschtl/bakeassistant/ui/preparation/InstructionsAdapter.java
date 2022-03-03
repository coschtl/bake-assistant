package at.coschtl.bakeassistant.ui.preparation;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

import at.coschtl.bakeassistant.Instruction;
import at.coschtl.bakeassistant.InstructionCalculator;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.ui.LongClickPosition;

public class InstructionsAdapter extends ArrayAdapter<Instruction> {

    private final InstructionCalculator instructionCalculator;
    private final View.OnLongClickListener onLongClickListener;
    private final LongClickPosition<Instruction> aktLongClickPosition;

    public InstructionsAdapter(@NonNull Activity activity, int resource, @NonNull InstructionCalculator instructionCalculator, View.OnLongClickListener onLongClickListener) {
        super(activity, resource, instructionCalculator.calculateInstructions(new Date()));
        this.instructionCalculator = instructionCalculator;
        this.onLongClickListener = onLongClickListener;
        aktLongClickPosition = new LongClickPosition();
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
        updateRow(row, instruction);
        row.setOnLongClickListener(v -> {
            Activity activity = (Activity) getContext();
            View selectTime = activity.findViewById(R.id.selectTime);
            View preparation = activity.findViewById(R.id.preparation);
            new TimeSetter(selectTime, new SettableTime.InstructionRowSettableTime(row, instruction), preparation).show();
            return false;
        });
        return row;
    }

     static void updateRow(LinearLayout row, Instruction instruction) {
        setText(R.id.time_min, instruction.getTimeMin().toString(), row);
        if (instruction.hasTimespan()) {
            row.findViewById(R.id.spacer).setVisibility(View.VISIBLE);
            setText(R.id.time_max, instruction.getTimeMax().toString(), row);
        } else {
            row.findViewById(R.id.spacer).setVisibility(View.GONE);
            setText(R.id.time_max, "", row);
        }
        setText(R.id.time_max, instruction.hasTimespan() ? instruction.getTimeMax().toString() : "", row);
        setText(R.id.action, instruction.getAction(), row);
        ((CheckBox) row.findViewById(R.id.done)).setChecked(instruction.isDone());
        row.forceLayout();
    }

    public LongClickPosition<Instruction> getAktLongClickPosition() {
        return aktLongClickPosition;
    }

    private static void setText(int id, String text, View parent) {
        ((TextView) parent.findViewById(id)).setText(text);
    }

}
