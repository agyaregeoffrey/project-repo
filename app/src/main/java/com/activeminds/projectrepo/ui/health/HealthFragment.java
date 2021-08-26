package com.activeminds.projectrepo.ui.health;

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
import com.activeminds.projectrepo.models.HealthProjectInfo;
import com.activeminds.projectrepo.adapters.HealthRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HealthFragment extends Fragment {

    private String TAG = "HealthFragment";
    private String HEALTH = "FHAS";

    private HealthRecyclerAdapter mRecyclerAdapter;
    private ArrayList<HealthProjectInfo> mProjectInfos;
    private RecyclerView mRecyclerView;

    private FirebaseFirestore mFirebaseFirestore;
    private View mRoot;
    public HealthFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRoot = inflater.inflate(R.layout.fragment_health, container, false);
        init();
        loadDataFromFirebase();
        return mRoot;
    }

    private void init () {
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mProjectInfos = new ArrayList<>();
        mRecyclerView = mRoot.findViewById(R.id.rv_health_projects);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
    }

    private void loadDataFromFirebase () {
        if (mProjectInfos.size() > 0)
            mProjectInfos.clear();
        mFirebaseFirestore.collection(Constants.PROJECTS_COLLECTION_PATH)
                .document(Constants.FACULTIES_DOCUMENT_PATH)
                .collection(HEALTH)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                HealthProjectInfo projectInfo = new HealthProjectInfo(documentSnapshot.getString(Constants.PROJECT_TITLE),
                                        documentSnapshot.getString(Constants.FACULTY), documentSnapshot.getString(documentSnapshot.getString(Constants.PROJECT_YEAR)));
                                projectInfo.setDownloadUrl(Constants.FILE_URL);
                                mProjectInfos.add(projectInfo);
                            }
                        } else {
                            // do something
                        }
                        mRecyclerAdapter = new HealthRecyclerAdapter(mProjectInfos, getContext());
                        mRecyclerAdapter.notifyDataSetChanged();
                        mRecyclerView.setAdapter(mRecyclerAdapter);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error: " + e);
            }
        });
    }
}
