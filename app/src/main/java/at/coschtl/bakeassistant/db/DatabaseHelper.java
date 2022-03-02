package at.coschtl.bakeassistant.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import at.coschtl.bakeassistant.ui.main.BakeAssistant;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "bakeassistant";
    public static final int DATABASE_VERSION = 1;

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
        db.execSQL(ConfigDbAdapter.DB.DATABASE_CREATE);
        createIndices(db);
    }

    private void createIndices(SQLiteDatabase db) {
        db.execSQL(RecipeDbAdapter.DB_ACTION.DATABASE_CREATE_INDEX_NAME);
        db.execSQL(RecipeDbAdapter.DB_RECIPE.DATABASE_CREATE_INDEX_NAME);
        db.execSQL(RecipeDbAdapter.DB_STEPS.DATABASE_CREATE_INDEX);
        db.execSQL(ConfigDbAdapter.DB.DATABASE_CREATE_INDEX_PROPERTY);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createBaseTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
