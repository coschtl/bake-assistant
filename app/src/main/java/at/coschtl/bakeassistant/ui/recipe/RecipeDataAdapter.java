package at.coschtl.bakeassistant.ui.recipe;

import java.util.List;

import at.coschtl.bakeassistant.db.RecipeDbAdapter;
import at.coschtl.bakeassistant.model.Action;
import at.coschtl.bakeassistant.model.Recipe;

public class RecipeDataAdapter {

    private final RecipeDbAdapter recipeDbAdapter;


    private Recipe recipe;

    public RecipeDataAdapter(long recipeId) {
        recipeDbAdapter = new RecipeDbAdapter();
        loadRecipe(recipeId);
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public Recipe loadRecipe(long recipeId) {
        if (recipeId >= 0) {
            recipe = recipeDbAdapter.getRecipe(recipeId);
        } else {
            recipe = new Recipe(-1, null);
        }
        return recipe;
    }

    public Action getOrCreateAction(String name) {
        Action action = recipeDbAdapter.getAction(name);
        if (action == null) {
            action = recipeDbAdapter.add(new Action(name));
        }
        return action;
    }

    public void saveRecipe() {
        Recipe saved = recipeDbAdapter.save(recipe);
        if (saved.getId() != recipe.getId()) {
            recipe = saved;
        }
    }

    public List<Action> loadActions() {
        return recipeDbAdapter.findAllActions();
    }

    public void finish() {
        if (recipeDbAdapter != null) {
            recipeDbAdapter.close();
        }
    }
}
