package com.activeminds.projectrepo.ui.projects;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.activeminds.projectrepo.R;
import com.activeminds.projectrepo.constants.Constants;
import com.activeminds.projectrepo.models.ProjectInfo;
import com.activeminds.projectrepo.adapters.ProjectRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProjectsFragment extends Fragment {

    public static final String TAG = "ProjectsFragment";
    private String[] collections = {"ENGINEERING",
            "FBMS", "FAST",
            "FHAS", "FBNE"};

    private ArrayList<ProjectInfo> mProjectInfos;
    private ProjectRecyclerAdapter mProjectAdapter;
    private RecyclerView mRecyclerView;
    private FirebaseFirestore mFirebaseFirestore;
    private DocumentReference mDocumentReference;

    private View mRoot;

    public ProjectsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRoot = inflater.inflate(R.layout.fragment_projects, container, false);
        init();

        // read data from all faculties
        for(String collection : collections){
            loadFromFirebase(collection);
        }
        return mRoot;
    }

    private void init() {
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mProjectInfos = new ArrayList<>();
        mRecyclerView = mRoot.findViewById(R.id.rv_all_projects);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
                        if(task.isSuccessful()) {
                            for(DocumentSnapshot documentSnapshot : task.getResult()) {
                                ProjectInfo projectInfo = new ProjectInfo(documentSnapshot.getString(Constants.PROJECT_TITLE),
                                        documentSnapshot.getString(Constants.FACULTY), documentSnapshot.getString(Constants.PROJECT_YEAR));
                                projectInfo.setDownloadUrl(documentSnapshot.getString(Constants.FILE_URL));
                                mProjectInfos.add(projectInfo);
                            }
                        }else {
                            Log.d(TAG, "Unable to read");
                        }
                        mProjectAdapter = new ProjectRecyclerAdapter(mProjectInfos, getContext());
                        mProjectAdapter.notifyDataSetChanged();
                        mRecyclerView.setAdapter(mProjectAdapter);
                    }
                });
    }
}
