package at.coschtl.bakeassistant.ui.recipe;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.model.Step;

public class RecipeStepsAdapter extends RecyclerView.Adapter<RecipeStepsAdapter.ViewHolder> {

    private final StepEditor stepEditor;
    private List<Step> steps;
    private final LongClickPosition aktLongClickPosition;

    public RecipeStepsAdapter(List<Step> dataSet, StepEditor stepEditor) {
        steps = new ArrayList<>(dataSet);
        this.stepEditor = stepEditor;
        aktLongClickPosition = new LongClickPosition();
    }

    public void moveStep(int from, int to) {
        steps.add(to, steps.remove(from));
        notifyItemMoved(from, to);
    }

    public void deleteStep(int position) {
        steps.remove(position);
        notifyItemRemoved(position);
    }

    public void setData(List<Step> dataSet) {
        steps = new ArrayList<>(dataSet);
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_row, viewGroup, false);

        return new ViewHolder(view, stepEditor, aktLongClickPosition);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.setStep(steps.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return steps.size();
    }

    public LongClickPosition getAktLongClickPosition() {
        return aktLongClickPosition;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        public static final int MENU_EDIT = 0;
        public static final int MENU_UP = MENU_EDIT + 1;
        public static final int MENU_DOWN = MENU_UP + 1;

        private final TextView textView;
        private final StepEditor stepEditor;
        private Step step;

        public ViewHolder(View view, StepEditor stepEditor, LongClickPosition aktLongClickPosition) {
            super(view);
            this.stepEditor = stepEditor;

            textView = view.findViewById(R.id.step_name);
            textView.setOnLongClickListener(v -> {
                aktLongClickPosition.setStep(step);
                return false;
            });
            textView.setOnCreateContextMenuListener(this);
        }

        public void setStep(Step step) {
            this.step = step;
            textView.setText(step.toString());
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int pos = 0;
            menu.add(Menu.NONE, MENU_EDIT, pos++, R.string.menu_edit);
            menu.add(Menu.NONE, MENU_UP, pos++, R.string.menu_up);
            menu.add(Menu.NONE, MENU_DOWN, pos++, R.string.menu_down);
        }

    }

    public static class LongClickPosition {
        private Step step;

        public Step getStep() {
            return step;
        }

        public void setStep(Step step) {
            this.step = step;
        }
    }
}
