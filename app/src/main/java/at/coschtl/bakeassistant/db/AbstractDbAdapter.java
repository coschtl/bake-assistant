package at.coschtl.bakeassistant.db;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class AbstractDbAdapter {

    private final SQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;
    private boolean closed = true;

    public AbstractDbAdapter() {
        dbHelper = DatabaseHelper.INSTANCE;
    }

    public static void close(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public static Map<String, Integer> createColMapping(String[] colNames) {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        int i = 0;
        for (String col : colNames) {
            mapping.put(col, i++);
        }
        return mapping;
    }

    public void close() {
        dbHelper.close();
        closed = true;
    }

    protected SQLiteDatabase db() {
        openIfNecessary();
        return db;
    }

    public void forceUpgrade(int versionTo) {
        dbHelper.onUpgrade(db(), 0, versionTo);
    }

    public boolean isClosed() {
        return closed;
    }

    /**
     * Open the database. If it cannot be opened, try to create a new instance of the database. If it cannot be created, throw an exception to signal the
     * failure
     *
     * @return this (self reference, allowing this to be chained in an initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public AbstractDbAdapter openIfNecessary() throws SQLException {
        if (isClosed() || !db.isOpen()) {
            db = dbHelper.getWritableDatabase();
            closed = false;
        }
        return this;
    }

    interface ModelObjectBuilder<T> {
        T buildObject(Cursor cursor);
    }

}
