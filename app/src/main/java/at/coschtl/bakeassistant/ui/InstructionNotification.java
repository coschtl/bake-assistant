package at.coschtl.bakeassistant.ui;

import static at.coschtl.bakeassistant.util.UiUtil.setText;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import at.coschtl.bakeassistant.Instruction;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;
import at.coschtl.bakeassistant.util.SerializationUtil;

public class InstructionNotification extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_INSTRUCTION = "instruction";
    private static final String ALARM_URI = "android.resource://" + BakeAssistant.PKG + "/" + R.raw.alarm;

    private final MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm);
        Bundle extras = getIntent().getExtras();
        byte[] instructionAsBytes = extras.getByteArray(BakeAssistant.PKG_PREF + EXTRA_INSTRUCTION);
        Instruction instruction = SerializationUtil.deserialize(instructionAsBytes);

        if (instruction.hasAlarm()) {
            setText(R.id.step_name, instruction.getAction(), InstructionNotification.this);
            setText(R.id.step_timespan, instruction.getTimespanString(), InstructionNotification.this);
            Button sleepButton = findViewById(R.id.sleep);
            sleepButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    Intent intent = new Intent(getApplicationContext(), InstructionNotification.class);
                    intent.putExtra(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_INSTRUCTION, instructionAsBytes);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    Calendar cal = Calendar.getInstance();
                   // cal.add(Calendar.SECOND, 10);
                   cal.add(Calendar.MINUTE, 3);
                    ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                    finish();
                }
            });
            Button doneButton = findViewById(R.id.done);
            doneButton.setOnClickListener(InstructionNotification.this);

            try {
                mediaPlayer.setDataSource(this, Uri.parse(ALARM_URI));
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            sendBroadcastAndFinish();
        }
    }

    @Override
    public void onClick(View v) {
        mediaPlayer.stop();
        mediaPlayer.release();
        sendBroadcastAndFinish();
    }

    private void sendBroadcastAndFinish() {
        Intent intent = new Intent(InstructionNotification.class.getName());
        sendBroadcast(intent);
        finish();
    }
}
