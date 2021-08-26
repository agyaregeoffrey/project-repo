package com.activeminds.projectrepo.ui.built;

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
import com.activeminds.projectrepo.models.BuiltProjectInfo;
import com.activeminds.projectrepo.adapters.BuiltRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class BuiltFragment extends Fragment {

    private String TAG = "BuiltFragment";
    private String BUILT = "FBNE";

    private BuiltRecyclerAdapter mBuiltRecyclerAdapter;
    private ArrayList<BuiltProjectInfo> mProjectInfos;
    private RecyclerView mRecyclerView;

    private FirebaseFirestore mFirebaseFirestore;
    private View mRoot;

    public BuiltFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRoot = inflater.inflate(R.layout.fragment_built, container, false);
        init();
        loadDataFromFirebase();
        return mRoot;
    }

    private void init () {
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mProjectInfos = new ArrayList<>();
        mRecyclerView = mRoot.findViewById(R.id.rv_built_projects);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
    }

    private void loadDataFromFirebase () {
        if (mProjectInfos.size() > 0)
            mProjectInfos.clear();
        mFirebaseFirestore.collection(Constants.PROJECTS_COLLECTION_PATH)
                .document(Constants.FACULTIES_DOCUMENT_PATH)
                .collection(BUILT)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                BuiltProjectInfo projectInfo = new BuiltProjectInfo(documentSnapshot.getString(Constants.PROJECT_TITLE),
                                        documentSnapshot.getString(Constants.FACULTY), documentSnapshot.getString(Constants.PROJECT_YEAR));
                                projectInfo.setDownloadUrl(Constants.FILE_URL);
                                mProjectInfos.add(projectInfo);
                            }
                        } else {
                            // do something
                        }
                        mBuiltRecyclerAdapter = new BuiltRecyclerAdapter(mProjectInfos, getContext());
                        mBuiltRecyclerAdapter.notifyDataSetChanged();
                        mRecyclerView.setAdapter(mBuiltRecyclerAdapter);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error: " + e);
            }
        });
    }
}
