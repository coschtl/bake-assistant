package at.coschtl.bakeassistant.model;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;

public enum DurationUnit implements Serializable {

    MINUTES(R.string.minutes, 1),
    HOURS(R.string.hours, 60);

    private static final Map<String, DurationUnit> BY_LABEL;

    static {
        BY_LABEL = new HashMap<>();
        for (DurationUnit unit : DurationUnit.values()) {
            BY_LABEL.put(unit.toString(), unit);
        }
    }

    private final int labelId;
    private final long minutes;
    private String label;

    DurationUnit(int labelId, long minutes) {
        this.labelId = labelId;
        label = BakeAssistant.CONTEXT == null ? null : BakeAssistant.CONTEXT.getResources().getString(labelId);
        this.minutes = minutes;
    }

    public static DurationUnit byLabel(String label) {
        return BY_LABEL.get(label);
    }

    @Override
    public String toString() {
        return label == null ? super.toString() : label;
    }

    public String getLabel(Context context) {
        if (label == null) {
            label = context.getResources().getString(labelId);
        }
        return label;
    }

    public long getMinutes() {
        return minutes;
    }

    public long getSeconds() {
        return minutes * 60L;
    }

    public int getLabelId() {
        return labelId;
    }
}
