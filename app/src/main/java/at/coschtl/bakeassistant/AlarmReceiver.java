package at.coschtl.bakeassistant;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import at.coschtl.bakeassistant.ui.InstructionNotification;


public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent uiIntent = new Intent(context, InstructionNotification.class);
        uiIntent.putExtras(intent);
        uiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(uiIntent);
    }
}