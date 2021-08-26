package com.activeminds.projectrepo.ui.downloads;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.activeminds.projectrepo.R;
import com.activeminds.projectrepo.constants.Constants;
import com.activeminds.projectrepo.models.DownloadedProjectInfo;
import com.activeminds.projectrepo.adapters.DownloadedProjectsRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadsFragment extends Fragment {

    private String TAG = "DownloadsFragment";
    private String DOWNLOADS = "downloads";

    private DownloadedProjectsRecyclerAdapter mRecyclerAdapter;
    private ArrayList<DownloadedProjectInfo> mProjectInfos;
    private RecyclerView mRecyclerView;

    private FirebaseFirestore mFirebaseFirestore;
    private View mRoot;
    private Group mGroup;

    public DownloadsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRoot = inflater.inflate(R.layout.fragment_downloads, container, false);
        init();
        loadDataFromFirebase();
        return mRoot;
    }

    private void init() {
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mProjectInfos = new ArrayList<>();
        mRecyclerView = mRoot.findViewById(R.id.rv_downloaded_projects);
        mGroup = mRoot.findViewById(R.id.group_no_download);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
    }

    private void loadDataFromFirebase() {
        if (mProjectInfos.size() > 0)
            mProjectInfos.clear();

        mFirebaseFirestore.collection(Constants.DOWNLOADED_PROJECTS_PATH)
                .document(Constants.DOWNLOADED_DOCUMENT_PATH)
                .collection(Constants.USER_ID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                DownloadedProjectInfo projectsInfo = new DownloadedProjectInfo(documentSnapshot.getString(Constants.PROJECT_TITLE),
                                        documentSnapshot.getString(Constants.FACULTY), documentSnapshot.getString(Constants.YEAR));
                                projectsInfo.setDownloadUrl(documentSnapshot.getString(Constants.DOWNLOAD_URL));
                                projectsInfo.setProjectId(documentSnapshot.getId());
                                mProjectInfos.add(projectsInfo);
                            }
                        }

                        if (!mProjectInfos.isEmpty()) {
                            mGroup.setVisibility(View.INVISIBLE);
                            mRecyclerAdapter = new DownloadedProjectsRecyclerAdapter(mProjectInfos, getContext());
                            mRecyclerAdapter.notifyDataSetChanged();
                            mRecyclerView.setAdapter(mRecyclerAdapter);
                        }
                    }
                });
    }
}
