package com.activeminds.projectrepo.models;

public class HealthProjectInfo {
    private String projectId;
    private String mProjectTitle;
    private String mFaculty;
    private String mYear;
    private String downloadUrl;

    public HealthProjectInfo(String projectTitle, String faculty, String year) {
        mProjectTitle = projectTitle;
        mFaculty = faculty;
        mYear = year;
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
