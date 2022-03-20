package at.coschtl.bakeassistant.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.db.RecipeDbAdapter;
import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.ui.cfg.ConfigurationActivity;
import at.coschtl.bakeassistant.ui.recipe.EditRecipe;

public class BakeAssistant extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    public static final String EXTRA_RECIPE_ID = "extraRecipeId";
    public static Context CONTEXT;
    public static String PKG;
    public static String PKG_PREF;
    private RecipeDbAdapter recipeDbAdapter;
    private RecyclerView recipesListView;
    private TextView noRecipesTextView;
    private List<Recipe> recipes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (CONTEXT == null) {
            CONTEXT = getApplicationContext();
            PKG = getApplicationContext().getPackageName();
            PKG_PREF = PKG + ".";
        }

        setContentView(R.layout.bake_assistant);
        recipeDbAdapter = new RecipeDbAdapter();
        recipes = new ArrayList<>();

        noRecipesTextView = findViewById(R.id.recipes_no_recipes);
        recipesListView = findViewById(R.id.recipes_listview);

        RecipeAdapter adapter = new RecipeAdapter(recipes, this::edit);
        recipesListView.setAdapter(adapter);
        recipesListView.setLayoutManager(new LinearLayoutManager(this));
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int adapterPosition = viewHolder.getAdapterPosition();
                adapter.deleteRecipe(adapterPosition);
                Recipe removed = recipes.remove(adapterPosition);
                recipeDbAdapter.deleteRecipe(removed);
            }
        });
        itemTouchHelper.attachToRecyclerView(recipesListView);

        findViewById(R.id.new_recipe_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDb();
                Intent intent = new Intent(BakeAssistant.this, EditRecipe.class);
                intent.putExtra(PKG_PREF + EXTRA_RECIPE_ID, -1L);
                startActivityForResult(intent, 1);
            }
        });
        loadRecipes();
    }

    public void edit(long recipeId) {
        Intent intent = new Intent(BakeAssistant.this, EditRecipe.class);
        intent.putExtra(BakeAssistant.PKG_PREF + BakeAssistant.EXTRA_RECIPE_ID, recipeId);
        startActivityForResult(intent, 1);
    }

    private void loadRecipes() {
        recipes.clear();
        recipes.addAll(recipeDbAdapter.findAllRecipes(false));
        if (recipes.isEmpty()) {
            noRecipesTextView.setVisibility(View.VISIBLE);
            recipesListView.setVisibility(View.GONE);
        } else {
            noRecipesTextView.setVisibility(View.GONE);
            recipesListView.setVisibility(View.VISIBLE);
            ((RecipeAdapter) recipesListView.getAdapter()).setData(recipes);
        }
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.main_settings_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDb();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
    }

    private void closeDb() {
        if (recipeDbAdapter != null) {
            recipeDbAdapter.close();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                closeDb();
                Intent intent = new Intent(BakeAssistant.this, ConfigurationActivity.class);
                startActivityForResult(intent, 1);
                return true;
            default:
                return false;
        }
    }
}
