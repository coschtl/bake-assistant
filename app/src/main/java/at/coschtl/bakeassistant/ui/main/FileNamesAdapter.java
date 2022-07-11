package at.coschtl.bakeassistant.ui.main;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import at.coschtl.bakeassistant.ImportExport;
import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.db.RecipeDbAdapter;

public class FileNamesAdapter extends RecyclerView.Adapter<FileNamesAdapter.ViewHolder> {

    private final List<String> fileNames;
    private final Activity activity;
    private final RecipeDbAdapter recipeDbAdapter;

    public FileNamesAdapter(List<String> fileNames, Activity activity, RecipeDbAdapter recipeDbAdapter) {
        this.fileNames = new ArrayList<>(fileNames);
        this.activity = activity;
        this.recipeDbAdapter = recipeDbAdapter;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.file_name_row, viewGroup, false);
        return new ViewHolder(view, activity, recipeDbAdapter);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.setFileName(fileNames.get(position));
    }

    @Override
    public int getItemCount() {
        return fileNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;


        public ViewHolder(View view, Activity activity, RecipeDbAdapter recipeDbAdapter) {
            super(view);
            textView = view.findViewById(R.id.file_name);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File f = new File(activity.getFilesDir(), textView.getText().toString());
                    System.out.println("Importing from " + f.getAbsolutePath());
                    try (InputStream in = new FileInputStream(f)) {
                        new ImportExport(activity, recipeDbAdapter).importDb(in);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    activity.finish();
                }
            });
        }

        public void setFileName(String fileName) {
            textView.setText(fileName);
        }
    }
}
