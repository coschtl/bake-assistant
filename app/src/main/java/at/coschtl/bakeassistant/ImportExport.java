package at.coschtl.bakeassistant;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import at.coschtl.bakeassistant.db.RecipeDbAdapter;
import at.coschtl.bakeassistant.model.Action;
import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.model.Step;

public class ImportExport {

    private final Context context;
    private final RecipeDbAdapter recipeDbAdapter;

    public ImportExport(Context context, RecipeDbAdapter recipeDbAdapter) {
        this.context = context;
        this.recipeDbAdapter = recipeDbAdapter;
    }

    @NonNull
    public static String createFilename() {
        Calendar cal = Calendar.getInstance();
        StringBuilder filename = new StringBuilder();
        add(cal.get(Calendar.YEAR), 4, filename);
        add(cal.get(Calendar.MONTH) + 1, 2, filename);
        add(cal.get(Calendar.DAY_OF_MONTH), 2, filename);
        filename.append("_");
        add(cal.get(Calendar.HOUR_OF_DAY), 2, filename);
        add(cal.get(Calendar.MINUTE), 2, filename);
        add(cal.get(Calendar.SECOND), 2, filename);
        filename.append(".json");
        return filename.toString();
    }

    private static void add(int i, int digits, StringBuilder b) {
        String iString = Integer.toString(i);
        for (int j = 0; j < digits - iString.length(); j++) {
            b.append("0");
        }
        b.append(iString);
    }

    public void exportDb(String filename) throws IOException {
        List<Recipe> allRecipes = recipeDbAdapter.findAllRecipes(true);
        Gson gson = new Gson();
        String json = gson.toJson(allRecipes);
        System.out.println("exporting to " + filename);
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    public void importDb(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        Gson gson = new Gson();
        Recipe[] recipes = gson.fromJson(reader, Recipe[].class);
        System.out.println("got " + recipes.length + " recipes: " + Arrays.asList(recipes));
        recipeDbAdapter.clearDatabase();
        for (Recipe imported : recipes) {
            Recipe recipe = new Recipe(imported.getName());
            recipe.addSteps(imported.getSteps());
            recipeDbAdapter.save(recipe);
            for (Step step : recipe.getSteps()) {
                Action action = recipeDbAdapter.getAction(step.getAction().getName());
                if (action == null) {
                    recipeDbAdapter.add(step.getAction());
                }
            }
        }
    }
}
