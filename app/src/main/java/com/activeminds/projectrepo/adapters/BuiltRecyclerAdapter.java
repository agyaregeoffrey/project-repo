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
import com.activeminds.projectrepo.models.BuiltProjectInfo;
import com.activeminds.projectrepo.utils.DownloadUtils;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class BuiltRecyclerAdapter extends RecyclerView.Adapter<BuiltRecyclerAdapter.BuiltViewHolder> {
    private ArrayList<BuiltProjectInfo> mProjectInfos;
    private Context mContext;

    public BuiltRecyclerAdapter(ArrayList<BuiltProjectInfo> projectInfos, Context context) {
        mProjectInfos = projectInfos;
        mContext = context;
    }

    @NonNull
    @Override
    public BuiltRecyclerAdapter.BuiltViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_built_project, parent, false);

        return new BuiltViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuiltRecyclerAdapter.BuiltViewHolder holder, int position) {
        BuiltProjectInfo projectInfo = mProjectInfos.get(position);
        holder.bind(projectInfo);
        holder.mCurrentPosition = position;
    }

    @Override
    public int getItemCount() {
        return mProjectInfos.size();
    }

    public class BuiltViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTextProjectTitle;
        private final TextView mTextFaculty;
        private final TextView mTextProjectYear;
        private final ImageView mImageViewDownload;
        public int mCurrentPosition;

        public BuiltViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextProjectTitle = itemView.findViewById(R.id.text_project_title);
            mTextFaculty = itemView.findViewById(R.id.text_faculty);
            mTextProjectYear = itemView.findViewById(R.id.text_project_year);
            mImageViewDownload = itemView.findViewById(R.id.imageView_more);

            downloadPDF();
        }

        public void bind(BuiltProjectInfo projectInfo){
            mTextProjectTitle.setText(projectInfo.getProjectTitle());
            mTextFaculty.setText(projectInfo.getFaculty());
            mTextProjectYear.setText(projectInfo.getYear());
        }



        private void downloadPDF() {
            mImageViewDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BuiltProjectInfo projectInfo = mProjectInfos.get(mCurrentPosition);
                    DownloadUtils.downloadProjectFile(v.getContext(), projectInfo.getProjectTitle(), Constants.FILE_EXTENSION, projectInfo.getDownloadUrl());
                    Snackbar.make(v, R.string.file_downloading, Snackbar.LENGTH_LONG)
                            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                            .show();
                }
            });
        }
    }
}
