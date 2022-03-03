package at.coschtl.bakeassistant;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import at.coschtl.bakeassistant.model.Step;
import at.coschtl.bakeassistant.util.Time;

public class Instruction {

    private final String action;
    private Time timeMin;
    private Time timeMax;
    private  Date min;
    private  Date max;
    private boolean done;

    public Instruction(Step step) {
        this(step.getAction().getName());
    }

    public Instruction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        if (hasTimespan()) {
            return getTimeMin() + " - " + getTimeMax() + ": " + action;
        }
        return getTimeMin() + ": " + action;
    }

    public void setTimeMax(Date timeMax) {
        this.timeMax = new Time(timeMax);
        this.max = timeMax;
    }

    public void setTimeMin(Date timeMin) {
        this.timeMin = new Time(timeMin);
        this.min = timeMin;
    }

    public boolean setExecutionTime(int hour, int minute) {
        Time time = Time.of(timeMin).setHour(hour).setMinute(minute);
        if (time.date().before(min) || time.date().after(max)) {
            return false;
        }
        timeMin = time;
        timeMax = time;
        return true;
    }

    public boolean hasTimespan() {
        return !Objects.equals(timeMin, timeMax);
    }

    public Time getTimeMax() {
        return timeMax;
    }

    public Time getTimeMin() {
        return timeMin;
    }

    public String getAction() {
        return action;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
