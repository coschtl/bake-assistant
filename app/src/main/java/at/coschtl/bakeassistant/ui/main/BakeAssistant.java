package at.coschtl.bakeassistant.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import at.coschtl.bakeassistant.ImportExport;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.db.RecipeDbAdapter;
import at.coschtl.bakeassistant.model.Recipe;
import at.coschtl.bakeassistant.ui.recipe.EditRecipe;

public class BakeAssistant extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extraRecipeId";
    public static final String TAG_BAKE_ASSISTANT = BakeAssistant.class.getName();
    public static final int RC_OVERLAY_PERMISSION = 1111;
    public static final int RC_CHOOSE_FILE = 22222;

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

        if (!hasOverlayRight()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(BakeAssistant.this);
            builder.setMessage(R.string.reason_manage_overlay_permissions).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivityForResult(intent, RC_OVERLAY_PERMISSION);
                }
            }).show();
        } else {
            initAndShowUi();
        }
    }

    private void initAndShowUi() {
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
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                adapter.deleteRecipe(adapterPosition);
                                Recipe removed = recipes.remove(adapterPosition);
                                recipeDbAdapter.deleteRecipe(removed);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                adapter.notifyDataSetChanged();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(BakeAssistant.this);
                builder.setMessage(R.string.really_delete_recipe).setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();
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

    private boolean hasOverlayRight() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_OVERLAY_PERMISSION) {
            if (!hasOverlayRight()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BakeAssistant.this);
                builder.setMessage(R.string.can_not_run_without_permission).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                }).show();
            } else {
                initAndShowUi();
            }
        } else if (requestCode == RC_CHOOSE_FILE && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
            InputStream in = null;
            try {
                ParcelFileDescriptor descriptor = BakeAssistant.this.getContentResolver().openFileDescriptor(selectedfile, "r");
                in = new FileInputStream(descriptor.getFileDescriptor());
                new ImportExport(BakeAssistant.this, recipeDbAdapter).importDb(in);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(BakeAssistant.this, "Can not import recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (requestCode == RC_CHOOSE_FILE && resultCode == RESULT_OK) {
            loadRecipes();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.importRecipes).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(BakeAssistant.this, FileSelector.class);
                startActivityForResult(intent, RC_CHOOSE_FILE);
                return true;
            }
        });
        menu.findItem(R.id.exportRecipes).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    String filename = ImportExport.createFilename();
                    new ImportExport(BakeAssistant.this, recipeDbAdapter).exportDb(filename);
                    Toast.makeText(BakeAssistant.this, "Export finished: " + filename, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(BakeAssistant.this, "Can not export recipes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDb();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasOverlayRight()) {
            loadRecipes();
        }
    }

    private void closeDb() {
        if (recipeDbAdapter != null) {
            recipeDbAdapter.close();
        }
    }
}
