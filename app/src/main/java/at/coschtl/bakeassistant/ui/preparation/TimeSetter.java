package at.coschtl.bakeassistant.ui.preparation;

import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import at.coschtl.bakeassistant.R;

public class TimeSetter implements View.OnClickListener {
    private final View selectTime;
    private final TimePicker timePicker;
    private final SettableTime settableTime;
    private final View parent;

    public TimeSetter(View selectTime, SettableTime settableTime, View parent) {
        this.selectTime = selectTime;
        this.settableTime = settableTime;
        this.timePicker = selectTime.findViewById(R.id.timePicker);
        this.timePicker.setIs24HourView(true);
        this.parent = parent;
    }

    public void show() {
        parent.setVisibility(View.GONE);
        selectTime.setVisibility(View.VISIBLE);
        timePicker.setCurrentHour(settableTime.getHour());
        timePicker.setCurrentMinute(settableTime.getMinute());
        Button timeOk = selectTime.findViewById(R.id.setTime);
        timeOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        boolean success = settableTime.setTime(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        if (success) {
            parent.setVisibility(View.VISIBLE);
            selectTime.setVisibility(View.GONE);
        } else {
            Toast.makeText(parent.getContext(), "out of range", Toast.LENGTH_SHORT).show();
        }
    }
}
