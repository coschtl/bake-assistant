package at.coschtl.bakeassistant.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import at.coschtl.bakeassistant.R;
import at.coschtl.bakeassistant.db.RecipeDbAdapter;

public class FileSelector extends AppCompatActivity {

    private RecyclerView filesListView;
    private TextView noFilesTextView;
    private List<String> fileNames;
    private RecipeDbAdapter recipeDbAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);
        fileNames = Arrays.asList(fileList());
        Collections.sort(fileNames);
        Collections.reverse(fileNames);
        recipeDbAdapter = new RecipeDbAdapter();
        initAndShowUi();
    }

    @Override
    public void finish() {
        if (recipeDbAdapter != null) {
            recipeDbAdapter.close();
        }
        super.finish();
    }

    private void initAndShowUi() {
        noFilesTextView = findViewById(R.id.files_no_file);
        filesListView = findViewById(R.id.files_listview);

        FileNamesAdapter adapter = new  FileNamesAdapter(fileNames, this, recipeDbAdapter);
        filesListView.setAdapter(adapter);
        filesListView.setLayoutManager(new LinearLayoutManager(this));
        if (fileNames.isEmpty()) {
            noFilesTextView.setVisibility(View.VISIBLE);
        } else {
            filesListView.setVisibility(View.VISIBLE);
        }
    }

}
