package at.coschtl.bakeassistant.ui.preparation;

import static at.coschtl.bakeassistant.util.UiUtil.setText;

import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.coschtl.bakeassistant.Instruction;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.util.Day;

public class TimeSetter implements View.OnClickListener {
    private final PrepareRecipe activity;
    private final InstructionsAdapter instructionsAdapter;
    private final LinearLayout row;
    private final int position;
    private final Spinner datePicker;
    private final TimePicker timePicker;

    public TimeSetter(PrepareRecipe activity, InstructionsAdapter instructionsAdapter, LinearLayout row, int position) {
        this.activity = activity;
        this.instructionsAdapter = instructionsAdapter;
        this.row = row;
        this.position = position;

        timePicker = activity.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        ArrayAdapter<Day> days = new ArrayAdapter<>(activity, R.layout.spinner_item, getDays(instruction().getTimeMin().date()));
        datePicker = activity.findViewById(R.id.datePicker);
        datePicker.setAdapter(days);
        datePicker.setSelection(3);
    }

    private Instruction instruction() {
        return instructionsAdapter.getItem(position);
    }

    public void show() {
        activity.showTimeSelectionUi();
        activity.findViewById(R.id.setTime);

        Instruction instruction = instruction();
        setText(R.id.step_name, instruction().getAction(), activity);
        StringBuilder b = new StringBuilder(instruction.getTimeMin().toString());
        if (instruction.hasTimespan()) {
            b.append(" - ").append(instruction.getTimeMax().toString());
        }
        setText(R.id.step_planned, b.toString(), activity);
        timePicker.setCurrentHour(instruction.getTimeMin().hour());
        timePicker.setCurrentMinute(instruction.getTimeMin().minute());
        Button timeOk = activity.findViewById(R.id.setTime);
        timeOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (datePicker.getVisibility() == View.VISIBLE) {
            instruction().setExecutionTime((Day)datePicker.getSelectedItem(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        } else {
            instruction().setExecutionTime(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        }
        instructionsAdapter.notifyInstructionChanged(position);
        activity.hideTimeSelectionUi();
    }

    private List<Day> getDays(Date base) {
        List<Day> days = new ArrayList<>(11);
        for (int i=-3; i<7; i++) {
            days.add( Day.getDayRelativeTo(base, i));
        }
        return days;
    }
}
