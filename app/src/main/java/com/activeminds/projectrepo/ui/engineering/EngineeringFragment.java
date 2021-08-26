package com.activeminds.projectrepo.ui.engineering;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activeminds.projectrepo.R;
import com.activeminds.projectrepo.constants.Constants;
import com.activeminds.projectrepo.models.EngineeringProjectInfo;
import com.activeminds.projectrepo.adapters.EngineeringRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class EngineeringFragment extends Fragment {

    private static final String ENGINEERING = "ENGINEERING";
    private static final String TAG = "EngineeringFragment";

    private EngineeringRecyclerAdapter mEngineeringRecyclerAdapter;
    private ArrayList<EngineeringProjectInfo> mEngineProjects;
    private RecyclerView mEngineProjectRecyclerView;

    private FirebaseFirestore mFirebaseFirestore;
    private View mRoot;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_engineering, container, false);
        init();
        loadDataFromFirebase();
        return mRoot;
    }

    private void init() {
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mEngineProjects = new ArrayList<>();
        mEngineProjectRecyclerView = mRoot.findViewById(R.id.rv_engineering_projects);
        mEngineProjectRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mEngineProjectRecyclerView.setHasFixedSize(true);
    }

    private void loadDataFromFirebase() {
        if(mEngineProjects.size() > 0)
            mEngineProjects.clear();
        mFirebaseFirestore.collection(Constants.PROJECTS_COLLECTION_PATH)
                .document(Constants.FACULTIES_DOCUMENT_PATH)
                .collection(ENGINEERING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(DocumentSnapshot documentSnapshot : task.getResult()) {
                                EngineeringProjectInfo projectInfo = new EngineeringProjectInfo(documentSnapshot.getString(Constants.PROJECT_TITLE),
                                        documentSnapshot.getString(Constants.FACULTY), documentSnapshot.getString(Constants.PROJECT_YEAR));
                                projectInfo.setDownloadUrl(documentSnapshot.getString(Constants.FILE_URL));
                                mEngineProjects.add(projectInfo);
                            }
                        } else {
                            Log.d(TAG, "Error");
                        }
                        mEngineeringRecyclerAdapter = new EngineeringRecyclerAdapter(mEngineProjects,
                                getContext());
                        mEngineeringRecyclerAdapter.notifyDataSetChanged();
                        mEngineProjectRecyclerView.setAdapter(mEngineeringRecyclerAdapter);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error " + e);
            }
        });
    }
}
