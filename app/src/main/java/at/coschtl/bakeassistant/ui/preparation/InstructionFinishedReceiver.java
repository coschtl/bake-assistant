package at.coschtl.bakeassistant.ui.preparation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.logging.Level;
import java.util.logging.Logger;

import at.coschtl.bakeassistant.ui.main.BakeAssistant;

public class InstructionFinishedReceiver extends BroadcastReceiver {
    private static final Logger LOGGER = Logger.getLogger(InstructionFinishedReceiver.class.getName());

    private final AlarmStarter alarmStarter;

    public InstructionFinishedReceiver(AlarmStarter alarmStarter) {
        this.alarmStarter = alarmStarter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("broadcast received: " + intent.getAction());
        }
        int adjustSeconds = intent.getIntExtra(BakeAssistant.PKG_PREF + PrepareRecipe.EXTRA_ADJUST_TIME_SECONDS, 0);
        alarmStarter.startNextAlarm(adjustSeconds);
    }
}
