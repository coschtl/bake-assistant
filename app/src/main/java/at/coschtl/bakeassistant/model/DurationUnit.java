package at.coschtl.bakeassistant.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;

public enum DurationUnit implements Serializable {

   MINUTES(R.string.minutes, 1),
   HOURS(R.string.hours, 60);

   private static Map<String, DurationUnit> BY_LABEL;
   static {
       BY_LABEL = new HashMap<>();
       for (DurationUnit unit : DurationUnit.values()) {
           BY_LABEL.put(unit.toString(), unit);
       }
   }
   public static DurationUnit byLabel(String label) {
       return BY_LABEL.get(label);
   }

    private final int labelId;
    private final String label;
    private final long minutes;

    DurationUnit(int labelId, long minutes) {
        this.labelId = labelId;
        label = BakeAssistant.CONTEXT.getResources().getString(labelId);
        this.minutes = minutes;
    }

    @Override
    public String toString() {
       return label;
    }

    public String getLabel() {
        return label;
    }

    public long getMinutes() {
        return minutes;
    }

    public int getLabelId() {
        return labelId;
    }
}
