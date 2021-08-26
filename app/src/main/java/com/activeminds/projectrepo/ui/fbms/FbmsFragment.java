package com.activeminds.projectrepo.ui.fbms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.activeminds.projectrepo.R;
import com.activeminds.projectrepo.constants.Constants;
import com.activeminds.projectrepo.models.FbmsProjectInfo;
import com.activeminds.projectrepo.adapters.FbmsRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FbmsFragment extends Fragment {

    private FbmsRecyclerAdapter mRecyclerAdapter;
    private ArrayList<FbmsProjectInfo> mProjectInfos;
    private RecyclerView mRecyclerView;
    private View mRoot;

    private FirebaseFirestore mFirebaseFirestore;
    private static String FBMS = "FBMS";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mRoot = inflater.inflate(R.layout.fragment_fbms, container, false);
        init();
        loadDataFromFirebase();
        return mRoot;
    }

    private void init() {
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mProjectInfos = new ArrayList<>();
        mRecyclerView = mRoot.findViewById(R.id.rv_fbms_projects);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
    }

    private void loadDataFromFirebase() {
        if(mProjectInfos.size() > 0)
            mProjectInfos.clear();

        mFirebaseFirestore.collection(Constants.PROJECTS_COLLECTION_PATH)
                .document(Constants.FACULTIES_DOCUMENT_PATH)
                .collection(FBMS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(DocumentSnapshot documentSnapshot : task.getResult()) {
                                FbmsProjectInfo projectInfo = new FbmsProjectInfo(documentSnapshot.getString(Constants.PROJECT_TITLE),
                                        documentSnapshot.getString(Constants.FACULTY),
                                        documentSnapshot.getString(Constants.PROJECT_YEAR));
                                projectInfo.setDownloadUrl(Constants.FILE_URL);
                                mProjectInfos.add(projectInfo);
                            }
                        }else{
                            // do something
                        }
                        mRecyclerAdapter = new FbmsRecyclerAdapter(mProjectInfos, getContext());
                        mRecyclerAdapter.notifyDataSetChanged();
                        mRecyclerView.setAdapter(mRecyclerAdapter);
                    }
                });
    }
}
