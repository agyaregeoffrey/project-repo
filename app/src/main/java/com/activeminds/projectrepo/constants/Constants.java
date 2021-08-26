package com.activeminds.projectrepo.constants;

import com.google.firebase.auth.FirebaseAuth;

public class Constants {
    public static final String PROJECTS_COLLECTION_PATH = "projects";
    public static final String FACULTIES_DOCUMENT_PATH = "faculties";

    public static final String DOWNLOADED_PROJECTS_PATH = "downloaded";
    public static final String DOWNLOADED_DOCUMENT_PATH = "users";
    public static String USER_ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    // fields
    public static final String PROJECT_TITLE = "projectTitle";
    public static final String FACULTY = "faculty";
    public static final String PROJECT_YEAR = "projectYear";
    public static final String FILE_URL = "fileUrl";

    // download fields
    public static final String YEAR = "year";
    public static final String DOWNLOAD_URL = "downloadUrl";

    // others
    public static final String FILE_EXTENSION = ".pdf";

}
