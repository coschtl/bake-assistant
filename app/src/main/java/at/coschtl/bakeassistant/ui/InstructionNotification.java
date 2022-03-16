package at.coschtl.bakeassistant.ui;

import static at.coschtl.bakeassistant.util.UiUtil.setText;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.db.RecipeDbAdapter;
import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.ui.main.BakeAssistant;

public class InstructionNotification extends AppCompatActivity implements View.OnClickListener {
    public InstructionNotification() {
        System.out.println("CONSTRUCTOR");
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        System.out.println("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm);
        Bundle extras = getIntent().getExtras();
        long recipeId = extras.getLong(BakeAssistant.EXTRA_RECIPE_ID);
        RecipeDbAdapter recipeDbAdapter = new RecipeDbAdapter();
        Recipe recipe = recipeDbAdapter.getRecipe(recipeId);
        setText(R.id.step_name,recipe.getName(),this);
        Button doneButton = findViewById(R.id.done);
        doneButton.setOnClickListener(this);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(this, notification);
            mp.prepareAsync();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
