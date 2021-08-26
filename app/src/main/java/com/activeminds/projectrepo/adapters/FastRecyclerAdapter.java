package com.activeminds.projectrepo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.activeminds.projectrepo.R;
import com.activeminds.projectrepo.constants.Constants;
import com.activeminds.projectrepo.models.DownloadedProjectInfo;
import com.activeminds.projectrepo.models.FastProjectInfo;
import com.activeminds.projectrepo.utils.DownloadUtils;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FastRecyclerAdapter extends RecyclerView.Adapter<FastRecyclerAdapter.FastViewHolder>{
    private ArrayList<FastProjectInfo> mProjectInfos;
    private Context mContext;

    public FastRecyclerAdapter(ArrayList<FastProjectInfo> mProjectInfos, Context mContext) {
        this.mProjectInfos = mProjectInfos;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public FastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_fast_project, parent, false);
        return new FastViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FastViewHolder holder, int position) {
        FastProjectInfo projectInfo = mProjectInfos.get(position);
        holder.bind(projectInfo);
        holder.mCurrentPosition = position;
    }

    @Override
    public int getItemCount() {
        return mProjectInfos.size();
    }

    public class FastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final TextView mTextProjectTitle;
        private final TextView mTextFaculty;
        private final TextView mTextProjectYear;
        private final ImageView mImageViewDownload;
        public int mCurrentPosition;
        public FastViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextProjectTitle = itemView.findViewById(R.id.text_project_title);
            mTextFaculty = itemView.findViewById(R.id.text_faculty);
            mTextProjectYear = itemView.findViewById(R.id.text_project_year);
            mImageViewDownload = itemView.findViewById(R.id.imageView_more);
            itemView.setOnClickListener(this::onClick);
        }

        private void bind(FastProjectInfo projectInfo) {
            mTextProjectTitle.setText(projectInfo.getProjectTitle());
            mTextFaculty.setText(projectInfo.getFaculty());
            mTextProjectYear.setText(projectInfo.getYear());

            downloadPDF();
        }

        private void downloadPDF() {
            mImageViewDownload.setOnClickListener(v -> {
                FastProjectInfo projectInfo = mProjectInfos.get(mCurrentPosition);
                DownloadUtils.downloadProjectFile(v.getContext(), projectInfo.getProjectTitle(), Constants.FILE_EXTENSION, projectInfo.getDownloadUrl());
                Snackbar.make(v, R.string.file_downloading, Snackbar.LENGTH_LONG)
                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                        .show();

                // add downloaded file to downloads path
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                DownloadedProjectInfo saveFile = new DownloadedProjectInfo(projectInfo.getProjectTitle(),
                        projectInfo.getFaculty(), projectInfo.getYear(), projectInfo.getDownloadUrl());
                firebaseFirestore.collection(Constants.DOWNLOADED_PROJECTS_PATH)
                        .document(Constants.DOWNLOADED_DOCUMENT_PATH)
                        .collection(Constants.USER_ID)
                        .add(saveFile);
            });
        }

        @Override
        public void onClick(View v) {
            mCurrentPosition = getAdapterPosition();
            FastProjectInfo projectInfo = mProjectInfos.get(mCurrentPosition);
            DownloadUtils.openDocument(itemView.getContext(), projectInfo.getDownloadUrl(), "application/pdf");
        }
    }
}
