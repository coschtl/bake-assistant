package at.coschtl.bakeassistant.ui;

import static at.coschtl.bakeassistant.util.UiUtil.setText;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import at.coschtl.bakeassistant.Instruction;
import at.coschtl.bakeassistant.R;

public class InstructionNotification extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_INSTRUCTION = "Instruction";

    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        System.out.println("NOTI: onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm);
        Bundle extras = getIntent().getExtras();
        Instruction instruction = (Instruction) extras.get(EXTRA_INSTRUCTION);
        System.out.println("NOTI: instruction=" + instruction.getAction());

        setText(R.id.step_name, instruction.getAction(), InstructionNotification.this);
        setText(R.id.step_timespan, instruction.getTimespanString(), InstructionNotification.this);
        Button doneButton = findViewById(R.id.done);
        doneButton.setOnClickListener(InstructionNotification.this);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        try {
            mediaPlayer.setDataSource(this, notification);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        mediaPlayer.release();
        System.out.println("NOTI: done");
        Intent intent = new Intent(InstructionNotification.class.getName());
        sendBroadcast(intent);
        finish();
    }
}
