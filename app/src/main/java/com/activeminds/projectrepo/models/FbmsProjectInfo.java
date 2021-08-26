package com.activeminds.projectrepo.models;

public class FbmsProjectInfo {
    private String projectId;
    private String mProjectTitle;
    private String mFaculty;
    private String mYear;
    private String downloadUrl;

    public FbmsProjectInfo() {
    }

    public FbmsProjectInfo(String mProjectTitle, String mFaculty, String mYear) {
        this.mProjectTitle = mProjectTitle;
        this.mFaculty = mFaculty;
        this.mYear = mYear;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getProjectTitle() {
        return mProjectTitle;
    }

    public void setProjectTitle(String mProjectTitle) {
        this.mProjectTitle = mProjectTitle;
    }

    public String getFaculty() {
        return mFaculty;
    }

    public void setFaculty(String mFaculty) {
        this.mFaculty = mFaculty;
    }

    public String getYear() {
        return mYear;
    }

    public void setYear(String mYear) {
        this.mYear = mYear;
    }
}
