package at.coschtl.bakeassistant.ui.cfg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.cfg.ConfigurationEntry;

public class ConfigurationActivity extends Activity implements View.OnClickListener {

    private ConfigurationViewModel viewModel;
    private EditText weigh_in;
    private EditText forming;
    private EditText mixing;

    public static ConfigurationActivity newInstance() {
        return new ConfigurationActivity();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);
        findViewById(R.id.save).setOnClickListener(this);
        weigh_in = findViewById(R.id.weigh_in);
        forming = findViewById(R.id.forming);
        mixing = findViewById(R.id.mixing);
        viewModel = new ConfigurationViewModel();
        updateUi();
    }

    private void updateUi() {
        setText(weigh_in, viewModel.getConfiguration().getWeigh_in());
        setText(forming, viewModel.getConfiguration().getForming());
        setText(mixing, viewModel.getConfiguration().getMixing());
    }

    private void setText(EditText editText, ConfigurationEntry entry) {
        if (entry.getValue() == null) {
            editText.setText("");
        } else {
            editText.setText(entry.getValueAsString());
        }
    }

    @Override
    public void onClick(View v) {
        try {
            viewModel.getWeigh_in().setValue(weigh_in.getText().toString());
            viewModel.getForming().setValue(forming.getText().toString());
            viewModel.getMixing().setValue(mixing.getText().toString());
            viewModel.updateAction();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "At least one value is not valid!", Toast.LENGTH_LONG).show();
        }
    }
}