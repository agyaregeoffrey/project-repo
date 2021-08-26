package com.activeminds.projectrepo.models;

public class BuiltProjectInfo {
    private String mProjectTitle;
    private String mFaculty;
    private String mYear;
    private String downloadUrl;

    public BuiltProjectInfo(String mProjectTitle, String mFaculty, String mYear) {
        this.mProjectTitle = mProjectTitle;
        this.mFaculty = mFaculty;
        this.mYear = mYear;
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
