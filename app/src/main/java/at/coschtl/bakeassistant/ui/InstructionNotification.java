package at.coschtl.bakeassistant.ui;

import static at.coschtl.bakeassistant.util.UiUtil.setText;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import at.coschtl.bakeassistant.NotificationWorker;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;
import at.coschtl.bakeassistant.ui.preparation.PrepareRecipe;

public class InstructionNotification extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_HAS_ALARM = "hasAlarm";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_SUPPRESS_ADJUST_ROW = "suppressAdjustRow";
    public static final String EXTRA_TIMESPAN_STRING = "timespanString";
    public static final String EXTRA_SHOW_ADJUST_ROW = "showAdjustRow";
    private static final String ALARM_URI = "android.resource://" + BakeAssistant.PKG + "/" + R.raw.alarm;

    private static final Logger LOGGER = Logger.getLogger(InstructionNotification.class.getName());

    private MediaPlayer mediaPlayer;
    private long displayStartMillis;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LOGGER.fine("onCreate");
        super.onCreate(savedInstanceState);
        mediaPlayer = new MediaPlayer();
        displayStartMillis = System.currentTimeMillis();
        setContentView(R.layout.alarm);
        Bundle extras = getIntent().getExtras();
        boolean hasAlarm = extras.getBoolean(BakeAssistant.PKG_PREF + EXTRA_HAS_ALARM);
        String action = extras.getString(BakeAssistant.PKG_PREF + EXTRA_ACTION);
        String timespan = extras.getString(BakeAssistant.PKG_PREF + EXTRA_TIMESPAN_STRING);
        View adjust = findViewById(R.id.adjust_time_row);

        if (extras.getBoolean(BakeAssistant.PKG_PREF + EXTRA_SHOW_ADJUST_ROW, false)) {
            adjust.setVisibility(View.VISIBLE);
        } else {
            if (!extras.getBoolean(BakeAssistant.PKG_PREF + EXTRA_SUPPRESS_ADJUST_ROW, false)) {
                new Handler().postDelayed(() -> adjust.setVisibility(View.VISIBLE), 5000);
            }
        }

        if (hasAlarm) {
            setText(R.id.step_name, action, InstructionNotification.this);
            setText(R.id.step_timespan, timespan, InstructionNotification.this);
            ImageView sleepButton = findViewById(R.id.sleep);
            sleepButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alarmOff();
                    Data inputData = new Data.Builder()
                            .putBoolean(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_SHOW_ADJUST_ROW, true)
                            .putBoolean(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_HAS_ALARM, true)
                            .putString(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_ACTION, action)
                            .putString(BakeAssistant.PKG_PREF + InstructionNotification.EXTRA_TIMESPAN_STRING, timespan)
                            .build();
                    WorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                            .setInitialDelay(180, TimeUnit.SECONDS)
                            .setInputData(inputData)
                            .addTag(BakeAssistant.TAG_BAKE_ASSISTANT)
                            .build();
                    WorkManager.getInstance(getApplicationContext()).enqueue(uploadWorkRequest);
                    finish();
                }
            });
            ImageView silentButton = findViewById(R.id.silent);
            silentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alarmOff();
                    v.setVisibility(View.INVISIBLE);
                }
            });
            ImageView doneButton = findViewById(R.id.done);
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

    private void alarmOff() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        alarmOff();
        sendBroadcastAndFinish();
    }

    private void sendBroadcastAndFinish() {
        Intent intent = new Intent(InstructionNotification.class.getName());
        CheckBox adjust_time = findViewById(R.id.adjust_time);
        if (adjust_time.isChecked()) {
            int adjustSeconds = (int) ((System.currentTimeMillis() - displayStartMillis) / 1000);
            intent.putExtra(BakeAssistant.PKG_PREF + PrepareRecipe.EXTRA_ADJUST_TIME_SECONDS, adjustSeconds);
        }
        sendBroadcast(intent);
        finish();
    }
}

