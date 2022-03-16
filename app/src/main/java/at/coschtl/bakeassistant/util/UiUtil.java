package at.coschtl.bakeassistant.util;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

public class UiUtil {
    public static void setText(int id, String text, View parent) {
        ((TextView) parent.findViewById(id)).setText(text);
    }

    public static void setText(int id, String text, Activity activity) {
        ((TextView) activity.findViewById(id)).setText(text);
    }
}
