package com.activeminds.projectrepo.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;

import com.activeminds.projectrepo.constants.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;

public class DownloadUtils {
    public static FirebaseStorage storage;

    private DownloadUtils() {
        storage = FirebaseStorage.getInstance();
    }

    public static void downloadProjectFile(Context context, String fileName, String fileExtension, String url) {
        DownloadManager downloadManager = (DownloadManager) context
                .getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request downloadRequest = new DownloadManager.Request(uri);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
            downloadRequest.allowScanningByMediaScanner();
            downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + fileExtension);
        downloadManager.enqueue(downloadRequest);
    }



    public static void openDocument(Context context, String name, String m_type) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        File file = new File(name);
        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (extension.equalsIgnoreCase("") || mimeType == null) {
            // if there is no extension or there is no definite mimeType, still try to open the file //intent.setDataAndType(Uri.fromFile(file), "text/*");
            /*Uri apkURI = FileProvider.getUriForFile( getContext(), getActivity().getApplicationContext() .getPackageName() + ".provider", file);*/
            intent.setDataAndType(Uri.parse(name), m_type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            //intent.setDataAndType(Uri.fromFile(file), mimeType);
            /* Uri apkURI = FileProvider.getUriForFile( getContext(), getActivity().getApplicationContext() .getPackageName() + ".provider", file);*/
            //intent.setDataAndType(apkURI, mimeType);
            intent.setDataAndType(Uri.parse(name), m_type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        // custom message for the intent
        context.startActivity(Intent.createChooser(intent, "Choose an Application"));
    }


}
