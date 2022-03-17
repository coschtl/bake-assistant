package at.coschtl.bakeassistant;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import at.coschtl.bakeassistant.model.Step;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;
import at.coschtl.bakeassistant.util.Day;
import at.coschtl.bakeassistant.util.Time;

public class Instruction implements Serializable {

    private final String action;
    private final Step step;
    private Time timeMin;
    private Time timeMax;
    private boolean done;

    public static Instruction getStartInstruction(Date timeMin, Date timeMax) {
        return createInstance(timeMin, timeMax, R.string.step_start);
    }

    public static Instruction getEndInstruction(Date timeMin, Date timeMax) {
        return createInstance(timeMin, timeMax, R.string.step_done);
    }

    private static Instruction createInstance(Date timeMin, Date timeMax, int string) {
        Instruction instruction = new Instruction(BakeAssistant.CONTEXT.getString(string)) {
            @Override
            public boolean showInteraction() {
                return false;
            }
        };
        instruction.setTimeMin(timeMin);
        instruction.setTimeMax(timeMax);
        return instruction;
    }

    public Instruction(Step step) {
        this.step = step;
        this.action = step.getAction().toString();
    }

    public Instruction(String action) {
        this.step = null;
        this.action = action;
    }

    public Step getStep() {
        return step;
    }

    public boolean showInteraction() {
        return true;
    }

    @Override
    public String toString() {
        if (hasTimespan()) {
            return getTimeMin() + " - " + getTimeMax() + ": " + action;
        }
        return getTimeMin() + ": " + action;
    }


    public void setExecutionTime(Day day, int hour, int minute) {
        setExecutionTime(day.getDate(), hour, minute);
    }
    public void setExecutionTime( int hour, int minute) {
        setExecutionTime(timeMin.date(), hour, minute);
    }
    private void setExecutionTime(Date day, int hour, int minute) {
        Time time = Time.of(day).setHour(hour).setMinute(minute);
            timeMin = time;
            timeMax = time;
    }

    public boolean hasTimespan() {
        return !Objects.equals(timeMin, timeMax);
    }

    public String getTimespanString() {
        StringBuilder b = new StringBuilder(getTimeMin().toString());
        if (hasTimespan()) {
            b.append(" - ").append(getTimeMax().toString());
        }
        return b.toString();
    }

    public Time getTimeMax() {
        return timeMax;
    }

    public Time getTimeMin() {
        return timeMin;
    }

    public void setTimeMax(Date timeMax) {
        if (!done) {
            this.timeMax = new Time(timeMax);
        }
    }

    public void setTimeMin(Date timeMin) {
        if (!done) {
            this.timeMin = new Time(timeMin);
        }
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
