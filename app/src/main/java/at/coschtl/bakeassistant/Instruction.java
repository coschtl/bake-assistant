package at.coschtl.bakeassistant;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import at.coschtl.bakeassistant.model.Step;

public class Instruction {

    private final String action;
    private final DateFormat dateFormat;
    private Date timeMin;
    private Date timeMax;
    private boolean done;

    public Instruction(Step step) {
        this(step.getAction().getName());
    }

    public Instruction(String action) {
        this.action = action;
        dateFormat = new SimpleDateFormat("E HH:mm");
    }


    @Override
    public String toString() {
        if (hasTimespan()) {
            return getFormatedTimeMin() + " - " + getFormatedTimeMax() + ": " + action;
        }
        return getFormatedTimeMin() + ": " + action;
    }

    public void setTimeMax(Date timeMax) {
        this.timeMax = timeMax;
    }

    public void setTimeMin(Date timeMin) {
        this.timeMin = timeMin;
    }

    public void setExecutionTime(Date executionTime) {
        timeMin = executionTime;
        timeMax = executionTime;
    }

    public boolean hasTimespan() {
        return !Objects.equals(timeMin, timeMax);
    }

    public String getFormatedTimeMax() {
        return dateFormat.format(timeMax);
    }

    public String getFormatedTimeMin() {
        return dateFormat.format(timeMin);
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
