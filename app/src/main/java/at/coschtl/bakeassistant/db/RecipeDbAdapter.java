package at.coschtl.bakeassistant.db;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import at.coschtl.bakeassistant.model.Action;
import at.coschtl.bakeassistant.model.DurationUnit;
import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.model.Step;

/**
 * Simple property database access helper class.
 * <p>
 * This has been improved from the first version of this tutorial through the addition of better error handling and also using returning a Cursor instead of
 * using a collection of inner classes (which is less scalable and not recommended).
 */
public class RecipeDbAdapter extends AbstractDbAdapter implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(RecipeDbAdapter.class.getName());

    public Recipe save(Recipe recipe) {
        Recipe saved;
        if (recipe.getId() < 0) {
            long id = db().insert(DB_RECIPE.DATABASE_TABLE, null, toContentValues(recipe));
            saved = new Recipe(id, recipe.getName());
        } else {
            db().update(DB_RECIPE.DATABASE_TABLE, toContentValues(recipe), DB_RECIPE.COL_ID + "=?", new String[]{Long.toString(recipe.getId())});
            saved = new Recipe(recipe.getId(), recipe.getName()).addSteps(recipe.getSteps());
        }
        deleteAllSteps(saved);
        int stepId = 0;
        for (Step step : recipe.getSteps()) {
            step.setRecipeId(saved.getId());
            step.setId(stepId++);
            saved.getSteps().add(add(step));
        }
        return saved;
    }

    private void deleteAllSteps(Recipe recipe) {
        db().delete(DB_STEPS.DATABASE_TABLE, DB_STEPS.COL_RECIPE_ID + "=?", new String[]{Long.toString(recipe.getId())});
    }

    public void clearDatabase() {
        db().delete(DB_STEPS.DATABASE_TABLE, null, null);
        db().delete(DB_RECIPE.DATABASE_TABLE, null, null);
        db().delete(DB_ACTION.DATABASE_TABLE, null, null);
    }

    private Step add(Step step) {
        long id = db().insert(DB_STEPS.DATABASE_TABLE, null, toContentValues(step));
        return step.setId(id);
    }

    public void deleteRecipe(Recipe recipe) {
        deleteAllSteps(recipe);
        db().delete(DB_RECIPE.DATABASE_TABLE, DB_RECIPE.COL_ID + "=?", new String[]{Long.toString(recipe.getId())});
    }

    public Recipe getRecipe(long recipeId) {
        Cursor cursor = db().query(true, DB_RECIPE.DATABASE_TABLE, DB_RECIPE.COL_NAMES, DB_RECIPE.COL_ID + "=?", new String[]{Long.toString(recipeId)}, null, null, null, null);
        Recipe recipe = toModelObject(cursor, DB_RECIPE.MODEL_OBJECT_BUILDER);
        addSteps(recipe);
        return recipe;
    }

    public List<Recipe> findAllRecipes(boolean includeSteps) {
        Cursor cursor = db().query(false, DB_RECIPE.DATABASE_TABLE, DB_RECIPE.COL_NAMES, null, null, null, null, null, null);
        List<Recipe> recipes = toModelObjects(cursor, DB_RECIPE.MODEL_OBJECT_BUILDER);
        if (includeSteps) {
            for (Recipe recipe : recipes) {
                addSteps(recipe);
            }
        }
        return recipes;
    }

    private void addSteps(Recipe recipe) {
        Cursor cursor = db().query(false, DB_STEPS.DATABASE_TABLE, DB_STEPS.COL_NAMES, DB_STEPS.COL_RECIPE_ID + "=?",
                new String[]{Long.toString(recipe.getId())}, null, null, null, null);
        recipe.addSteps(toModelObjects(cursor, getStepObjectBuilder()));
    }

    public List<Action> findAllActions() {
        Cursor cursor = db().query(false, DB_ACTION.DATABASE_TABLE, DB_ACTION.COL_NAMES, null, null, null, null, null, null);
        return toModelObjects(cursor, DB_ACTION.MODEL_OBJECT_BUILDER);
    }

    public Action add(Action action) {
        long id = db().insert(DB_ACTION.DATABASE_TABLE, null, toContentValues(action));
        return new Action(id, action.getName());
    }

    public Action getAction(int actionId) {
        Cursor cursor = db().query(true, DB_ACTION.DATABASE_TABLE, DB_ACTION.COL_NAMES, DB_ACTION.COL_ID + "=?",
                new String[]{Integer.toString(actionId)}, null, null, null, null);
        return toModelObject(cursor, DB_ACTION.MODEL_OBJECT_BUILDER);
    }

    public Action getAction(String name) {
        Cursor cursor = db().query(true, DB_ACTION.DATABASE_TABLE, DB_ACTION.COL_NAMES, DB_ACTION.COL_NAME + "=?",
                new String[]{name}, null, null, null, null);
        return toModelObject(cursor, DB_ACTION.MODEL_OBJECT_BUILDER);
    }

    private <T> T toModelObject(Cursor cursor, ModelObjectBuilder<T> modelObjectBuilder) {
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return modelObjectBuilder.buildObject(cursor);
                }
            } finally {
                close(cursor);
            }
        }
        return null;
    }

    private <T> List<T> toModelObjects(Cursor cursor, ModelObjectBuilder<T> modelObjectBuilder) {
        List<T> objects = new ArrayList<>();
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        T object = modelObjectBuilder.buildObject(cursor);
                        objects.add(object);
                    } while (cursor.moveToNext());
                }
            } finally {
                close(cursor);
            }
        }
        return objects;
    }

    private ContentValues toContentValues(Action action) {
        ContentValues cvs = new ContentValues();
        if (action.getId() >= 0) {
            cvs.put(DB_ACTION.COL_ID, action.getId());
        }
        cvs.put(DB_ACTION.COL_NAME, action.getName());
        return cvs;
    }

    private ContentValues toContentValues(Recipe recipe) {
        ContentValues cvs = new ContentValues();
        if (recipe.getId() >= 0) {
            cvs.put(DB_RECIPE.COL_ID, recipe.getId());
        }
        cvs.put(DB_RECIPE.COL_NAME, recipe.getName());
        return cvs;
    }

    private ContentValues toContentValues(Step step) {
        ContentValues cvs = new ContentValues();
        if (step.getId() >= 0) {
            cvs.put(DB_STEPS.COL_STEP_ID, step.getId());
        }
        cvs.put(DB_STEPS.COL_RECIPE_ID, step.getRecipeId());
        cvs.put(DB_STEPS.COL_ACTION_ID, step.getAction().getId());
        cvs.put(DB_STEPS.COL_DURATION_MIN, step.getDurationMin());
        cvs.put(DB_STEPS.COL_DURATION_MAX, step.getDurationMax());
        cvs.put(DB_STEPS.COL_ALARM, step.isAlarm() ? 1 : 0);
        cvs.put(DB_STEPS.COL_DURATION_UNIT, step.getDurationUnit().name());
        return cvs;
    }

    private final ModelObjectBuilder<Step> getStepObjectBuilder() {
        return new ModelObjectBuilder<Step>() {
            @Override
            public Step buildObject(Cursor cursor) {
                Step step = new Step(cursor.getInt(DB_STEPS.COL_MAPPING.get(DB_STEPS.COL_RECIPE_ID)));
                step.setId(cursor.getInt(DB_STEPS.COL_MAPPING.get(DB_STEPS.COL_STEP_ID)));
                step.setDurationMin(cursor.getInt(DB_STEPS.COL_MAPPING.get(DB_STEPS.COL_DURATION_MIN)));
                step.setDurationMax(cursor.getInt(DB_STEPS.COL_MAPPING.get(DB_STEPS.COL_DURATION_MAX)));
                step.setAlarm(cursor.getInt(DB_STEPS.COL_MAPPING.get(DB_STEPS.COL_ALARM)) == 1);
                String unitString = cursor.getString(DB_STEPS.COL_MAPPING.get(DB_STEPS.COL_DURATION_UNIT));
                if (unitString != null) {
                    step.setDurationUnit(DurationUnit.valueOf(unitString));
                }
                step.setAction(RecipeDbAdapter.this.getAction(cursor.getInt(DB_STEPS.COL_MAPPING.get(DB_STEPS.COL_ACTION_ID))));
                return step;
            }
        };
    }

    public static class DB_ACTION {
        public static final String COL_NAME = "label";
        public static final String COL_ID = "id";
        public static final String DATABASE_TABLE = "actions";
        public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + COL_ID + " integer primary key autoincrement, " + COL_NAME + " text not null);";

        public static final String DATABASE_CREATE_INDEX_NAME = "create index if not exists INDEX_NAME on " + DATABASE_TABLE + " (" + COL_NAME + ")";

        private static final String[] COL_NAMES = new String[]{COL_ID, COL_NAME};
        private static final Map<String, Integer> COL_MAPPING = createColMapping(COL_NAMES);

        private static final ModelObjectBuilder<Action> MODEL_OBJECT_BUILDER = new ModelObjectBuilder<Action>() {
            @Override
            public Action buildObject(Cursor cursor) {
                return new Action(cursor.getInt(DB_ACTION.COL_MAPPING.get(DB_ACTION.COL_ID)), cursor.getString(DB_ACTION.COL_MAPPING.get(DB_ACTION.COL_NAME)));
            }
        };
    }

    public static class DB_RECIPE {
        public static final String COL_NAME = "name";
        public static final String COL_ID = "id";
        public static final String DATABASE_TABLE = "recipes";
        public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + COL_ID + " integer primary key autoincrement, " + COL_NAME + " text not null);";

        public static final String DATABASE_CREATE_INDEX_NAME = "create index if not exists INDEX_NAME on " + DATABASE_TABLE + " (" + COL_NAME + ")";

        private static final String[] COL_NAMES = new String[]{COL_ID, COL_NAME};
        private static final Map<String, Integer> COL_MAPPING = createColMapping(COL_NAMES);
        private static final ModelObjectBuilder<Recipe> MODEL_OBJECT_BUILDER = new ModelObjectBuilder<Recipe>() {
            @Override
            public Recipe buildObject(Cursor cursor) {
                return new Recipe(cursor.getInt(DB_RECIPE.COL_MAPPING.get(DB_RECIPE.COL_ID)), cursor.getString(DB_RECIPE.COL_MAPPING.get(DB_RECIPE.COL_NAME)));
            }
        };
    }

    public static class DB_STEPS {
        public static final String COL_RECIPE_ID = "recipeId";
        public static final String COL_STEP_ID = "stepId";
        public static final String COL_ACTION_ID = "actionId";
        public static final String COL_DURATION_MIN = "durationMin";
        public static final String COL_DURATION_MAX = "durationMax";
        public static final String COL_DURATION_UNIT = "durationUnit";
        public static final String COL_ALARM = "alarm";

        public static final String DATABASE_TABLE = "recipeSteps";
        public static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + COL_RECIPE_ID + " integer not null, " + COL_STEP_ID + " integer not null, "
                + COL_ACTION_ID + " integer not null, " + COL_DURATION_MIN + " integer not null, " + COL_DURATION_MAX + " integer not null, " + COL_DURATION_UNIT + " text not null, "
                + COL_ALARM + " integer default 0);";

        public static final String DATABASE_CREATE_INDEX = "create index if not exists INDEX_RECIPE_STEPS on " + DATABASE_TABLE + " (" + COL_RECIPE_ID + ", " + COL_STEP_ID + ")";

        private static final String[] COL_NAMES = new String[]{COL_RECIPE_ID, COL_STEP_ID, COL_ACTION_ID, COL_DURATION_MIN, COL_DURATION_MAX, COL_DURATION_UNIT, COL_ALARM};
        private static final Map<String, Integer> COL_MAPPING = createColMapping(COL_NAMES);
    }

}
