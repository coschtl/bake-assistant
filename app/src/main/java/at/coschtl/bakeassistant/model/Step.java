package at.coschtl.bakeassistant.model;

import java.io.Serializable;

public class Step implements Serializable {
    private long id;
    private long recipeId;
    private Action action;
    private int durationMin;
    private int durationMax;
    private DurationUnit durationUnit;
    private boolean alarm;


    public Step(long recipeId) {
        this.recipeId = recipeId;
    }

    public long getId() {
        return id;
    }

    public Step setId(long id) {
        this.id = id;
        return this;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public Step setRecipeId(long recipeId) {
        this.recipeId = recipeId;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public Step setAction(Action action) {
        this.action = action;
        return this;
    }

    public boolean isAlarm() {
        return alarm;
    }

    public Step setAlarm(boolean alarm) {
        this.alarm = alarm;
        return this;
    }

    public int getDurationMin() {
        return durationMin;
    }

    public Step setDurationMin(int durationMin) {
        this.durationMin = durationMin;
        return this;
    }

    public int getDurationMax() {
        return durationMax;
    }

    public Step setDurationMax(int durationMax) {
        this.durationMax = durationMax;
        return this;
    }

    public DurationUnit getDurationUnit() {
        return durationUnit;
    }

    public Step setDurationUnit(DurationUnit durationUnit) {
        this.durationUnit = durationUnit;
        return this;
    }

    @Override
    public String toString() {
        if (durationMin == durationMax) {
            return action.getName() + ": " + durationMin + " " + durationUnit;
        }
        return action.getName() + ": " + durationMin + " - " + durationMax + " " + durationUnit;
    }
}
