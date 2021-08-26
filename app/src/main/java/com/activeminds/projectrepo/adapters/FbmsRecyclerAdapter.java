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
import com.activeminds.projectrepo.models.FbmsProjectInfo;
import com.activeminds.projectrepo.utils.DownloadUtils;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class FbmsRecyclerAdapter extends RecyclerView.Adapter<FbmsRecyclerAdapter.FbmsViewHolder>{
    private ArrayList<FbmsProjectInfo> mProjectInfos;
    private Context mContext;

    public FbmsRecyclerAdapter(ArrayList<FbmsProjectInfo> mProjectInfos, Context mContext) {
        this.mProjectInfos = mProjectInfos;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public FbmsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_fbms_projects, parent, false);
        return new FbmsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FbmsViewHolder holder, int position) {
        FbmsProjectInfo projectInfo = mProjectInfos.get(position);
        holder.bind(projectInfo);
        holder.mCurrentPosition = position;
    }

    @Override
    public int getItemCount() {
        return mProjectInfos.size();
    }

    public class FbmsViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTextProjectTitle;
        private final TextView mTextFaculty;
        private final TextView mTextProjectYear;
        private final ImageView mImageViewDownload;
        public int mCurrentPosition;

        public FbmsViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextProjectTitle = itemView.findViewById(R.id.text_project_title);
            mTextFaculty = itemView.findViewById(R.id.text_faculty);
            mTextProjectYear = itemView.findViewById(R.id.text_project_year);
            mImageViewDownload = itemView.findViewById(R.id.imageView_more);

            downloadPDF();
        }

        public void bind(FbmsProjectInfo projectInfo) {
            mTextProjectTitle.setText(projectInfo.getProjectTitle());
            mTextFaculty.setText(projectInfo.getFaculty());
            mTextProjectYear.setText(projectInfo.getYear());
        }

        private void downloadPDF() {
            mImageViewDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   FbmsProjectInfo projectInfo = mProjectInfos.get(mCurrentPosition);
                    DownloadUtils.downloadProjectFile(v.getContext(), projectInfo.getProjectTitle(), Constants.FILE_EXTENSION, projectInfo.getDownloadUrl());
                    Snackbar.make(v, R.string.file_downloading, Snackbar.LENGTH_LONG)
                            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                            .show();
                }
            });
        }
    }
}
