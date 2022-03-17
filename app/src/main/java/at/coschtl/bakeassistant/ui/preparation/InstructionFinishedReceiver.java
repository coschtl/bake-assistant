package at.coschtl.bakeassistant.ui.preparation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstructionFinishedReceiver extends BroadcastReceiver {
    private final AlarmStarter alarmStarter;

    public InstructionFinishedReceiver(AlarmStarter alarmStarter) {
        this.alarmStarter = alarmStarter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("broadcast received: " + intent.getAction());
        alarmStarter.startNextAlarm();
    }
}
