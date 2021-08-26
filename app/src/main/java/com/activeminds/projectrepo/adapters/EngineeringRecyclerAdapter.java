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
import com.activeminds.projectrepo.models.EngineeringProjectInfo;
import com.activeminds.projectrepo.utils.DownloadUtils;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class EngineeringRecyclerAdapter extends RecyclerView.Adapter<EngineeringRecyclerAdapter.EngineeringViewHolder>{
    private ArrayList<EngineeringProjectInfo> mProjectInfos;
    private Context mContext;

    public EngineeringRecyclerAdapter(ArrayList<EngineeringProjectInfo> projectInfos, Context context) {
        mProjectInfos = projectInfos;
        mContext = context;
    }

    @NonNull
    @Override
    public EngineeringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_engineering_projects, parent, false);
        return new EngineeringViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EngineeringViewHolder holder, int position) {
        EngineeringProjectInfo projectInfo = mProjectInfos.get(position);
        holder.bind(projectInfo);
        holder.mCurrentPosition = position;
    }

    @Override
    public int getItemCount() {
        return mProjectInfos.size();
    }

    public class EngineeringViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTextProjectTitle;
        private final TextView mTextFaculty;
        private final TextView mTextProjectYear;
        private final ImageView mImageViewDownload;
        public int mCurrentPosition;
        public EngineeringViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextProjectTitle = itemView.findViewById(R.id.text_project_title);
            mTextFaculty = itemView.findViewById(R.id.text_faculty);
            mTextProjectYear = itemView.findViewById(R.id.text_project_year);
            mImageViewDownload = itemView.findViewById(R.id.imageView_more);

            downloadPDF();
        }

        public void bind(EngineeringProjectInfo projectInfo){
            mTextProjectTitle.setText(projectInfo.getProjectTitle());
            mTextFaculty.setText(projectInfo.getFaculty());
            mTextProjectYear.setText(projectInfo.getYear());
        }



        private void downloadPDF() {
            mImageViewDownload.setOnClickListener(v -> {
                EngineeringProjectInfo projectInfo = mProjectInfos.get(mCurrentPosition);
                DownloadUtils.downloadProjectFile(v.getContext(), projectInfo.getProjectTitle(), Constants.FILE_EXTENSION, projectInfo.getDownloadUrl());
                Snackbar.make(v, R.string.file_downloading, Snackbar.LENGTH_LONG)
                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                        .show();
            });
        }

    }
}
