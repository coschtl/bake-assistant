package at.coschtl.bakeassistant.cfg;

import java.util.HashMap;
import java.util.Map;

import at.coschtl.bakeassistant.db.ConfigDbAdapter;

public class Configuration {

    private static final String PROP_WEIGHT_IN = "weigh_in";
    private static final String PROP_MIXING = "mixing";
    private static final String PROP_FORMING = "forming";
    private static final Configuration INSTANCE;
    public static String[] PROPERTY_NAMES;

    static {
        PROPERTY_NAMES = new String[]{PROP_WEIGHT_IN, PROP_MIXING, PROP_FORMING};
        try (ConfigDbAdapter configDbAdapter = new ConfigDbAdapter()) {
            INSTANCE = new Configuration();
            configDbAdapter.readFromDatabase(INSTANCE);
        }
    }

    private final Map<String, ConfigurationEntry> entries;

    private Configuration() {
        entries = new HashMap<>();
        entries.put(PROP_WEIGHT_IN, new ConfigurationEntry<>(PROP_WEIGHT_IN, TypeConverter.INTEGER));
        entries.put(PROP_MIXING, new ConfigurationEntry<>(PROP_MIXING, TypeConverter.INTEGER));
        entries.put(PROP_FORMING, new ConfigurationEntry<>(PROP_FORMING, TypeConverter.INTEGER));
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public <T> ConfigurationEntry<T> getEntry(String name) {
        return entries.get(name);
    }

    public void updateProperty(String name, String value, String unit) {
        ConfigurationEntry entry = getEntry(name);
        entry.setValue(value);
        entry.setUnit(unit);
    }

    public ConfigurationEntry<Integer> getWeigh_in() {
        return entries.get(PROP_WEIGHT_IN);
    }

    public ConfigurationEntry<Integer> getMixing() {
        return entries.get(PROP_MIXING);
    }

    public ConfigurationEntry<Integer> getForming() {
        return entries.get(PROP_FORMING);
    }
}
