package at.coschtl.bakeassistant.ui.cfg;

import androidx.lifecycle.ViewModel;

import at.coschtl.bakeassistant.cfg.Configuration;
import at.coschtl.bakeassistant.cfg.ConfigurationEntry;
import at.coschtl.bakeassistant.db.ConfigDbAdapter;

public class ConfigurationViewModel extends ViewModel {
    private final Configuration configuration;

    public ConfigurationViewModel() {
        configuration = Configuration.getInstance();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ConfigurationEntry<Integer> getWeigh_in() {
        return configuration.getWeigh_in();
    }

    public ConfigurationEntry<Integer> getMixing() {
        return configuration.getMixing();
    }

    public ConfigurationEntry<Integer> getForming() {
        return configuration.getForming();
    }

    public void updateAction() {
        try (ConfigDbAdapter configDbAdapter = new ConfigDbAdapter()) {
            configDbAdapter.save(configuration);
        }
    }
}