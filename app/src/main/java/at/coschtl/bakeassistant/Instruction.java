package at.coschtl.bakeassistant;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import at.coschtl.bakeassistant.model.Step;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;
import at.coschtl.bakeassistant.util.Time;

public class Instruction implements Serializable {

    private final String action;
    private final Step step;
    private Time timeMin;
    private Time timeMax;
    private boolean done;
    private boolean active;

    public Instruction(Step step) {
        this.step = step;
        this.action = step.getAction().toString();
    }

    public Instruction(String action) {
        this.step = null;
        this.action = action;
    }

    public static Instruction getStartInstruction(Date timeMin, Date timeMax) {
        return createInstance(timeMin, timeMax, R.string.step_start, false, Type.FIRST);
    }

    public static Instruction getEndInstruction(Date timeMin, Date timeMax) {
        return createInstance(timeMin, timeMax, R.string.step_done, true, Type.LAST);
    }

    private static Instruction createInstance(Date timeMin, Date timeMax, int string, boolean alarm, Type type) {
        Instruction instruction = new Instruction(BakeAssistant.CONTEXT.getString(string)) {
            @Override
            public boolean showInteraction() {
                return false;
            }

            @Override
            public boolean hasAlarm() {
                return alarm;
            }

            @Override
            public Type getType() {
                return type;
            }
        };
        instruction.setTimeMin(timeMin);
        instruction.setTimeMax(timeMax);
        return instruction;
    }

    public Step getStep() {
        return step;
    }

    public int getDurationMinSeconds() {
        if (step == null) {
            return 0;
        }
        return (int) (getStep().getDurationMin() * getStep().getDurationUnit().getSeconds());
    }

    public int getDurationMaxSeconds() {
        if (step == null) {
            return 0;
        }
        return (int) (getStep().getDurationMax() * getStep().getDurationUnit().getSeconds());
    }

    public boolean showInteraction() {
        return true;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Type getType() {
        return Type.STEP;
    }

    @Override
    public String toString() {
        if (hasTimespan()) {
            return getTimeMin() + " - " + getTimeMax() + ": " + action;
        }
        return getTimeMin() + ": " + action;
    }

    public boolean hasAlarm() {
        return step != null && step.isAlarm();
    }


//    public void setExecutionTime(Day day, int hour, int minute) {
//        setExecutionTime(day.getDate(), hour, minute);
//    }
//
//    public void setExecutionTime(int hour, int minute) {
//        setExecutionTime(timeMin.date(), hour, minute);
//    }
//
//    private void setExecutionTime(Date day, int hour, int minute) {
//        Time time = Time.of(day).setHour(hour).setMinute(minute);
//        timeMin = time;
//        timeMax = time;
//    }
//
//    public void delayExecutionTimeBySeconds(int delaySeconds) {
//        timeMin.addSeconds(delaySeconds);
//        timeMax.addSeconds(delaySeconds);
//    }

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

    public void setTimeMax(Date timeMax) {
        if (!done) {
            this.timeMax = new Time(timeMax);
        }
    }

    public int getDurationSeconds() {
        if (step == null) {
            return 0;
        }
        return (int) ((step.getDurationMax() - step.getDurationMin()) * step.getDurationUnit().getMinutes() * 60);
    }

    public Time getTimeMin() {
        return timeMin;
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

    public enum Type {
        FIRST, LAST, STEP
    }
}
