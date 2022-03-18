package at.coschtl.bakeassistant.util;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

public class UiUtil {
    public static TextView setText(int id, String text, View parent) {
        TextView textView = parent.findViewById(id);
        textView.setText(text);
        return textView;
    }

    public static TextView setText(int id, String text, Activity activity) {
        TextView textView = activity.findViewById(id);
        textView.setText(text);
        return textView;
    }
}
