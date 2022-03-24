package at.coschtl.bakeassistant;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import at.coschtl.bakeassistant.ui.InstructionNotification;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;

public class NotificationWorker extends Worker {

    public NotificationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        Intent uiIntent = new Intent(getApplicationContext(), InstructionNotification.class);
        setBoolean(uiIntent, InstructionNotification.EXTRA_HAS_ALARM);
        setString(uiIntent, InstructionNotification.EXTRA_ACTION);
        setString(uiIntent, InstructionNotification.EXTRA_TIMESPAN_STRING);
        uiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(uiIntent);

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    private void setBoolean(Intent intent, String name) {
        intent.putExtra(BakeAssistant.PKG_PREF + name, getInputData().getBoolean(BakeAssistant.PKG_PREF + name, false));
    }

    private void setString(Intent intent, String name) {
        intent.putExtra(BakeAssistant.PKG_PREF + name, getInputData().getString(BakeAssistant.PKG_PREF + name));
    }
}

