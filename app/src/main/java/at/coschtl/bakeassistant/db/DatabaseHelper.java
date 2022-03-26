package at.coschtl.bakeassistant.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import at.coschtl.bakeassistant.ui.main.BakeAssistant;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "bakeassistant";
    public static final int DATABASE_VERSION = 3;

    public static final DatabaseHelper INSTANCE = new DatabaseHelper();

    private DatabaseHelper() {
        super(BakeAssistant.CONTEXT, DATABASE_NAME, null, DATABASE_VERSION);
        try {
            getWritableDatabase();
        } catch (Exception e) {
            System.exit(0);
        }
    }

    public void createBaseTables(SQLiteDatabase db) {
        db.execSQL(RecipeDbAdapter.DB_ACTION.DATABASE_CREATE);
        db.execSQL(RecipeDbAdapter.DB_RECIPE.DATABASE_CREATE);
        db.execSQL(RecipeDbAdapter.DB_STEPS.DATABASE_CREATE);
        createIndices(db);
    }

    private void createIndices(SQLiteDatabase db) {
        db.execSQL(RecipeDbAdapter.DB_ACTION.DATABASE_CREATE_INDEX_NAME);
        db.execSQL(RecipeDbAdapter.DB_RECIPE.DATABASE_CREATE_INDEX_NAME);
        db.execSQL(RecipeDbAdapter.DB_STEPS.DATABASE_CREATE_INDEX);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createBaseTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            db.execSQL("ALTER TABLE " + RecipeDbAdapter.DB_STEPS.DATABASE_TABLE + " ADD " + RecipeDbAdapter.DB_STEPS.COL_ALARM + " integer default 0");
        }
        if (oldVersion > 0 && oldVersion < 3) {
            db.execSQL("drop table configuration");
        }
    }
}
