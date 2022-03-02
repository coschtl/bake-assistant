package at.coschtl.bakeassistant.cfg;

import android.content.ContentValues;

public class ConfigurationEntry <T>{
    private final String propertyName;
    private final TypeConverter<T> typeConverter;

    public ConfigurationEntry(String propertyName, TypeConverter<T> typeConverter) {
        this.propertyName = propertyName;
        this.typeConverter = typeConverter;
    }
    private T value;
    private String unit;

    public void setValue(ContentValues cv, String key) {
        typeConverter.setValue(cv, key, value);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public T getValue() {
        return value;
    }

    public String getValueAsString() {
        return typeConverter.convertToString(value);
    }

    public void setValue(String value) {
        this.value = typeConverter.convert(value);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
