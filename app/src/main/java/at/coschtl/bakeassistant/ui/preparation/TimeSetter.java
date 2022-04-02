package at.coschtl.bakeassistant.ui.preparation;

import static at.coschtl.bakeassistant.util.UiUtil.setText;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import at.coschtl.bakeassistant.Instruction;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.util.Day;
import at.coschtl.bakeassistant.util.Time;

public class TimeSetter implements View.OnClickListener {
    private final PrepareRecipe activity;
    private final InstructionsAdapter instructionsAdapter;
    private final View row;
    private final int position;
    private final Spinner datePicker;
    private final TimePicker timePicker;

    public TimeSetter(PrepareRecipe activity, InstructionsAdapter instructionsAdapter, View row, int position) {
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

    private int getDayPosition(Date date) {
        for (int i=0; i<datePicker.getAdapter().getCount(); i++) {
            Day day = (Day) datePicker.getAdapter().getItem(i);
            if (day.toString().equals(new Day(date).toString())) {
                return i;
            }
        }
        return 3;
    }

    public void show() {
        activity.showTimeSelectionUi();
        activity.findViewById(R.id.setTime);

        Instruction instruction = instruction();
        setText(R.id.step_name, instruction().getAction(), activity);
        setText(R.id.step_planned, instruction.getTimespanString(), activity);
        if (activity.isPreparationRunning()) {
            Calendar cal = Calendar.getInstance();
            timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
            datePicker.setSelection(getDayPosition(cal.getTime()));
        } else {
            timePicker.setCurrentHour(instruction.getTimeMin().hour());
            timePicker.setCurrentMinute(instruction.getTimeMin().minute());
            datePicker.setSelection(getDayPosition(instruction.getTimeMin().date()));
        }
        Button timeOk = activity.findViewById(R.id.setTime);
        timeOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Time executionTime;
        Instruction instruction = instruction();
        if (datePicker.getVisibility() == View.VISIBLE) {
            executionTime =  Time.of(((Day) datePicker.getSelectedItem()).getDate());
        } else {
            executionTime =  Time.of(instruction.getTimeMin().date());
        }
        executionTime.setHour(timePicker.getCurrentHour()).setMinute(timePicker.getCurrentMinute()).setSecond(0);
        int diffSeconds = (int) ((executionTime.date().getTime() - instruction.getTimeMin().date().getTime()) /1000L);
        //FIXME: do not change steps which are already done
        instructionsAdapter.getInstructionCalculator().recalculateBySeconds(position, diffSeconds, activity.getMinimumStepSet());
        instructionsAdapter. notifyDataSetChanged();
        activity.hideTimeSelectionUi(position);
        if (!activity.isPreparationRunning()) {
            row.getRootView().findViewById(R.id.start_now_button).setVisibility(View.VISIBLE);
        }
    }

    private List<Day> getDays(Date base) {
        List<Day> days = new ArrayList<>(11);
        for (int i = -3; i < 7; i++) {
            days.add(Day.getDayRelativeTo(base, i));
        }
        return days;
    }
}
