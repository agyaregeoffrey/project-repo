package com.activeminds.projectrepo.adapters;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.activeminds.projectrepo.R;
import com.activeminds.projectrepo.constants.Constants;
import com.activeminds.projectrepo.models.DownloadedProjectInfo;
import com.activeminds.projectrepo.utils.DownloadUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

public class DownloadedProjectsRecyclerAdapter extends RecyclerView.Adapter<DownloadedProjectsRecyclerAdapter.DownloadedProjectsViewHolder> {
    private ArrayList<DownloadedProjectInfo> mDownloadedProjectInfos;
    private Context mContext;
    private View mItemView;

    public DownloadedProjectsRecyclerAdapter(ArrayList<DownloadedProjectInfo> downloadedProjectInfos, Context context) {
        mDownloadedProjectInfos = downloadedProjectInfos;
        mContext = context;
    }

    @NonNull
    @Override
    public DownloadedProjectsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        mItemView = LayoutInflater.from(context)
                .inflate(R.layout.item_downloaded_project_list, parent, false);
        return new DownloadedProjectsViewHolder(mItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadedProjectsViewHolder holder, int position) {
        DownloadedProjectInfo downloadedProjectInfo = mDownloadedProjectInfos.get(position);
        holder.bind(downloadedProjectInfo);
        holder.mCurrentPosition = position;
    }

    @Override
    public int getItemCount() {
        return mDownloadedProjectInfos.size();
    }

    public class DownloadedProjectsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mTextDownloadedProjectTitle;
        private final TextView mTextDownloadedFaculty;
        private final TextView mTextDownloadedProjectYear;
        private final ImageView mMoreItemsImageView;
        public int mCurrentPosition;

        public DownloadedProjectsViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextDownloadedProjectTitle = itemView.findViewById(R.id.text_downloaded_project_title);
            mTextDownloadedFaculty = itemView.findViewById(R.id.text_downloaded_faculty);
            mTextDownloadedProjectYear = itemView.findViewById(R.id.text_downloaded_project_year);
            mMoreItemsImageView = itemView.findViewById(R.id.imageView_more);
            itemView.setOnClickListener(this);

            // display options for more actions
            mMoreItemsImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setTitle(R.string.more)
                            .setPositiveButton(R.string.download_string, (dialog, which) -> {
                                downloadSelectedFile();
                            })
                            .setNegativeButton(R.string.delete_file, (dialog, which) -> {
                                deleteSelectedFile();
                            })
                            .show();
                }
            });
        }

        public void bind(DownloadedProjectInfo downloadedProjectInfo) {
            mTextDownloadedProjectTitle.setText(downloadedProjectInfo.getProjectTitle());
            mTextDownloadedFaculty.setText(downloadedProjectInfo.getFaculty());
            mTextDownloadedProjectYear.setText(downloadedProjectInfo.getYear());
        }

        private void downloadSelectedFile() {
            if (ContextCompat.checkSelfPermission(itemView.getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                DownloadedProjectInfo projectInfo = mDownloadedProjectInfos.get(mCurrentPosition);
                DownloadUtils.downloadProjectFile(itemView.getContext(), projectInfo.getProjectTitle(), Constants.FILE_EXTENSION, projectInfo.getDownloadUrl());
                Snackbar.make(itemView, R.string.file_downloading, Snackbar.LENGTH_LONG)
                        .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                        .show();
            } else {
                requestStoragePermission();
            }
        }

        @Override
        public void onClick(View v) {
            mCurrentPosition = getAdapterPosition();
            DownloadedProjectInfo projectInfo = mDownloadedProjectInfos.get(mCurrentPosition);
            DownloadUtils.openDocument(itemView.getContext(), projectInfo.getDownloadUrl(), "application/pdf");
        }

        private void deleteSelectedFile() {
            mCurrentPosition = getAdapterPosition();
            DownloadedProjectInfo projectInfo = mDownloadedProjectInfos.get(mCurrentPosition);
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection(Constants.DOWNLOADED_PROJECTS_PATH)
                    .document(Constants.DOWNLOADED_DOCUMENT_PATH)
                    .collection(Constants.USER_ID)
                    .document(projectInfo.getProjectId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Snackbar.make(mItemView, R.string.remove_file, Snackbar.LENGTH_LONG)
                            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                            .show());
        }
    }

    private void requestStoragePermission() {
        Dexter.withContext(mContext)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        if (permissionDeniedResponse.isPermanentlyDenied()) {
                            new MaterialAlertDialogBuilder(mContext)
                                    .setTitle("Permission")
                                    .setMessage("Storage access permissions are required to download files")
                                    .show();
                        } else {
                            Snackbar.make(mItemView, R.string.permission_denied, Snackbar.LENGTH_LONG)
                                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
                                    .show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }
}
