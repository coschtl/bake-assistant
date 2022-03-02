package at.coschtl.bakeassistant.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.model.Recipe;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private List<Recipe> recipes;
    private RecipeEditor recipeEditor;

    public RecipeAdapter(List<Recipe> dataSet, RecipeEditor recipeEditor) {
        recipes = new ArrayList<>(dataSet);
        this.recipeEditor=recipeEditor;
    }

    public void deleteRecipe(int position) {
        recipes.remove(position);
        notifyItemRemoved(position);
    }

    public void setData(List<Recipe> dataSet) {
        recipes = new ArrayList<>(dataSet);
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_row, viewGroup, false);

        return new ViewHolder(view, recipeEditor);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.setRecipe(recipes.get(position), position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return recipes.size();
    }


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private Recipe recipe;
        private int position;

        public ViewHolder(View view, RecipeEditor recipeEditor) {
            super(view);

            textView = (TextView) view.findViewById(R.id.step_name);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recipe != null) {
                        recipeEditor.edit( recipe.getId());
                    }
                }
            });
        }

        public void setRecipe(Recipe recipe, int position) {
            this.recipe = recipe;
            this.position = position;
            textView.setText(recipe.getName());
        }
    }
}
