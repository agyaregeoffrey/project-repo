package com.activeminds.projectrepo.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import com.activeminds.projectrepo.R;
import com.activeminds.projectrepo.adapters.ProjectRecyclerAdapter;
import com.activeminds.projectrepo.constants.Constants;
import com.activeminds.projectrepo.models.ProjectInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    public static final String SEARCH_ACTIVITY = "SearchActivity";
    private String[] collections = {"ENGINEERING",
            "FBMS", "FAST",
            "FHAS", "FBNE"};

    private ArrayList<ProjectInfo> mProjectInfos;
    private ProjectRecyclerAdapter mProjectAdapter;
    private RecyclerView mRecyclerView;
    private FirebaseFirestore mFirebaseFirestore;
    EditText mEditTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        // read data from all faculties
        for (String collection : collections) {
            loadFromFirebase(collection);
        }

        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mProjectInfos = new ArrayList<>();
        mRecyclerView = findViewById(R.id.rv_search_results);
        mEditTextSearch = findViewById(R.id.editText_search);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

    }

    private void loadFromFirebase(String collection) {
        mFirebaseFirestore.collection(Constants.PROJECTS_COLLECTION_PATH)
                .document(Constants.FACULTIES_DOCUMENT_PATH)
                .collection(collection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                ProjectInfo projectInfo = new ProjectInfo(documentSnapshot.getString(Constants.PROJECT_TITLE),
                                        documentSnapshot.getString(Constants.FACULTY), documentSnapshot.getString(Constants.PROJECT_YEAR));
                                projectInfo.setDownloadUrl(documentSnapshot.getString(Constants.FILE_URL));
                                mProjectInfos.add(projectInfo);
                            }
                        } else {
                            Log.d(SEARCH_ACTIVITY, "Unable to read");
                        }
                        mProjectAdapter = new ProjectRecyclerAdapter(mProjectInfos, getApplicationContext());
                        mProjectAdapter.notifyDataSetChanged();
                        mRecyclerView.setAdapter(mProjectAdapter);
                    }
                });
    }

    private void search(String search) {
        ArrayList<ProjectInfo> projectInfos = new ArrayList<>();
        for (ProjectInfo projectInfo : mProjectInfos) {
            if (projectInfo.getProjectTitle() != null && projectInfo.getProjectTitle().contains(search) ||
                projectInfo.getFaculty() != null && projectInfo.getFaculty().contains(search) ||
                projectInfo.getYear() != null && projectInfo.getYear().contains(search)) {
                projectInfos.add(projectInfo);
            }
        }
        mProjectAdapter = new ProjectRecyclerAdapter(projectInfos, getApplicationContext());
        mProjectAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mProjectAdapter);
    }
}
