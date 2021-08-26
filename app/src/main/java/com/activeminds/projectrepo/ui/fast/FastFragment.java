package com.activeminds.projectrepo.ui.fast;

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
import com.activeminds.projectrepo.models.FastProjectInfo;
import com.activeminds.projectrepo.adapters.FastRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FastFragment extends Fragment {

    private String TAG = "FastFragment";

    public static final String FAST = "FAST";
    private FastRecyclerAdapter mRecyclerAdapter;
    private ArrayList<FastProjectInfo> mFastProjects;
    private RecyclerView mRecyclerView;
    private View mRoot;
    private FirebaseFirestore mFirebaseFirestore;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mRoot = inflater.inflate(R.layout.fragment_fast, container, false);
        init();
        loadDataFromFirebase();
        return mRoot;
    }

    private void init() {
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFastProjects = new ArrayList<>();
        mRecyclerView = mRoot.findViewById(R.id.rv_fast_projects);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
    }

    private void loadDataFromFirebase() {
        if (mFastProjects.size() > 0)
            mFastProjects.clear();

        mFirebaseFirestore.collection(Constants.PROJECTS_COLLECTION_PATH)
                .document(Constants.FACULTIES_DOCUMENT_PATH)
                .collection(FAST)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                FastProjectInfo projectInfo = new FastProjectInfo(documentSnapshot.getString(Constants.PROJECT_TITLE),
                                        documentSnapshot.getString(Constants.FACULTY), documentSnapshot.getString(Constants.PROJECT_YEAR));
                                projectInfo.setDownloadUrl(documentSnapshot.getString(Constants.FILE_URL));
                                mFastProjects.add(projectInfo);
                            }
                        } else {
                            // do something
                        }
                        mRecyclerAdapter = new FastRecyclerAdapter(mFastProjects,
                                getContext());
                        mRecyclerAdapter.notifyDataSetChanged();
                        mRecyclerView.setAdapter(mRecyclerAdapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // report error
                        Log.d(TAG, "Error: " + e);
                    }
                });
    }
}
