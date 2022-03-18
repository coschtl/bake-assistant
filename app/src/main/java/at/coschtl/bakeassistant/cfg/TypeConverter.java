package at.coschtl.bakeassistant.cfg;

import android.content.ContentValues;

public interface TypeConverter<T> {

    TypeConverter<String> STRING = new TypeConverter<String>() {
        @Override
        public String convert(String value) {
            return value;
        }

        @Override
        public String convertToString(String value) {
            return value;
        }

        @Override
        public void setValue(ContentValues cv, String key, String value) {
            cv.put(key, value);
        }
    };
    TypeConverter<Integer> INTEGER = new TypeConverter<Integer>() {
        @Override
        public Integer convert(String value) {
            return Integer.valueOf(value);
        }

        @Override
        public String convertToString(Integer value) {
            return value.toString();
        }

        @Override
        public void setValue(ContentValues cv, String key, Integer value) {
            cv.put(key, value);
        }
    };

    T convert(String value);

    String convertToString(T value);

    void setValue(ContentValues cv, String key, T value);


}
