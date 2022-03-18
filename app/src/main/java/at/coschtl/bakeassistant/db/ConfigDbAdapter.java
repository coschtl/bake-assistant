package at.coschtl.bakeassistant.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Map;
import java.util.logging.Logger;

import at.coschtl.bakeassistant.cfg.Configuration;
import at.coschtl.bakeassistant.cfg.ConfigurationEntry;

/**
 * Simple property database access helper class.
 * <p>
 * This has been improved from the first version of this tutorial through the addition of better error handling and also using returning a Cursor instead of
 * using a collection of inner classes (which is less scalable and not recommended).
 */
public class ConfigDbAdapter extends AbstractDbAdapter implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(ConfigDbAdapter.class.getName());

    public void readFromDatabase(Configuration cfg) {
        Cursor cursor = db().query(false, DB.DATABASE_TABLE, DB.COL_NAMES, null, null, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        updateConfigurationEntry(cursor, cfg);
                    } while (cursor.moveToNext());
                }
            } finally {
                close(cursor);
            }
        }
    }

    public void save(Configuration config) {
        for (String name : Configuration.PROPERTY_NAMES) {
            ConfigurationEntry entry = config.getEntry(name);
            updateDb(entry);

        }
    }

    private void updateDb(ConfigurationEntry entry) {
        ContentValues cvs = toContentValues(entry);
        int u = db().update(DB.DATABASE_TABLE, cvs, DB.COL_PROPERTY_NAME + "=?", new String[]{entry.getPropertyName()});
        if (u == 0) {
            db().insertWithOnConflict(DB.DATABASE_TABLE, null, cvs, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    private ContentValues toContentValues(ConfigurationEntry entry) {
        ContentValues cvs = new ContentValues();
        cvs.put(DB.COL_PROPERTY_NAME, entry.getPropertyName());
        entry.setValue(cvs, DB.COL_VALUE);
        cvs.put(DB.COL_UNIT, entry.getUnit());
        return cvs;
    }

    private void updateConfigurationEntry(Cursor cursor, Configuration config) {
        if (cursor != null && !cursor.isClosed()) {
            String propertyName = cursor.getString(DB.COL_MAPPING.get(DB.COL_PROPERTY_NAME));
            String value = cursor.getString(DB.COL_MAPPING.get(DB.COL_VALUE));
            String unit = cursor.getString(DB.COL_MAPPING.get(DB.COL_UNIT));
            config.updateProperty(propertyName, value, unit);
        }
    }

    public static class DB {

        public static final String COL_PROPERTY_NAME = "propertyName";
        public static final String COL_VALUE = "value";
        public static final String COL_UNIT = "unit";
        public static final String DATABASE_TABLE = "configuration";
        public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + COL_PROPERTY_NAME + " text primary key not null, " + COL_VALUE + " text not null, " + COL_UNIT + " text);";

        public static final String DATABASE_CREATE_INDEX_PROPERTY = "create index if not exists INDEX_NAME on " + DATABASE_TABLE + " (" + COL_PROPERTY_NAME + ")";

        public static final String[] COL_NAMES = new String[]{COL_PROPERTY_NAME, COL_VALUE, COL_UNIT};
        public static final Map<String, Integer> COL_MAPPING = createColMapping(COL_NAMES);
    }

}
