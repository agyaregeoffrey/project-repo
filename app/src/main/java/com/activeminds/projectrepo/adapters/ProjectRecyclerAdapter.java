package com.activeminds.projectrepo.adapters;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.activeminds.projectrepo.constants.Constants;
import com.activeminds.projectrepo.models.DownloadedProjectInfo;
import com.activeminds.projectrepo.models.ProjectInfo;
import com.activeminds.projectrepo.R;
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

public class ProjectRecyclerAdapter extends RecyclerView.Adapter<ProjectRecyclerAdapter.ProjectViewHolder> {
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 13;

    private ArrayList<ProjectInfo> mProjectInfos;
    private Context mContext;

    public ProjectRecyclerAdapter(ArrayList<ProjectInfo> projectInfo, Context context) {
        mProjectInfos = projectInfo;
        mContext = context;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_project_list, parent, false);
        return new ProjectViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectInfo projectInfo = mProjectInfos.get(position);
        holder.bind(projectInfo);
        holder.mCurrentPosition = position;
    }

    @Override
    public int getItemCount() {
        return mProjectInfos.size();
    }

    public class ProjectViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mTextProjectTitle;
        private final TextView mTextFaculty;
        private final TextView mTextProjectYear;
        private final ImageView mImageViewDownload;
        public int mCurrentPosition;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextProjectTitle = itemView.findViewById(R.id.text_project_title);
            mTextFaculty = itemView.findViewById(R.id.text_faculty);
            mTextProjectYear = itemView.findViewById(R.id.text_project_year);
            mImageViewDownload = itemView.findViewById(R.id.imageView_more);
            itemView.setOnClickListener(this);
        }

        public void bind(ProjectInfo projectInfo) {
            mTextProjectTitle.setText(projectInfo.getProjectTitle());
            mTextFaculty.setText(projectInfo.getFaculty());
            mTextProjectYear.setText(projectInfo.getYear());


            downloadPDF();

        }

        @Override
        public void onClick(View v) {
            mCurrentPosition = getAdapterPosition();
            ProjectInfo projectInfo = mProjectInfos.get(mCurrentPosition);
            DownloadUtils.openDocument(itemView.getContext(), projectInfo.getDownloadUrl(), "application/pdf");
        }

        private void downloadPDF() {
            mImageViewDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    // check for granted permissions
                    if (ContextCompat.checkSelfPermission(v.getContext(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        new MaterialAlertDialogBuilder(itemView.getContext())
                                .setTitle(R.string.download_string)
                                .setMessage(R.string.download_file)
                                .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ProjectInfo projectInfo = mProjectInfos.get(mCurrentPosition);
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
                                    }
                                })
                                .setNegativeButton(R.string.cancel, null)
                                .show();
                        return;
                    } else {
                       requestStoragePermission();
                    }
                }
            });
        }

        private void requestStoragePermission () {
            Dexter.withContext(mContext)
                    .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            if(permissionDeniedResponse.isPermanentlyDenied()) {
                               new MaterialAlertDialogBuilder(mContext)
                                       .setTitle("Permission")
                                       .setMessage("Storage access permissions are required to download files")
                                       .show();
                            } else {
                                Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_LONG)
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


}
