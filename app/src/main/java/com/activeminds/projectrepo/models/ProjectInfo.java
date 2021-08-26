package com.activeminds.projectrepo.models;

public class ProjectInfo {
    private String projectId;
    private String mProjectTitle;
    private String mFaculty;
    private String mYear;
    private String downloadUrl;
    private String fileName;

    public ProjectInfo() {
    }

    public ProjectInfo(String projectTitle, String faculty, String year) {
        mProjectTitle = projectTitle;
        mFaculty = faculty;
        mYear = year;
    }

    public ProjectInfo (String fileName) {
        this.fileName = fileName;
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

    public void setProjectTitle(String projectTitle) {
        mProjectTitle = projectTitle;
    }

    public String getFaculty() {
        return mFaculty;
    }

    public void setFaculty(String faculty) {
        mFaculty = faculty;
    }

    public String getYear() {
        return mYear;
    }

    public void setYear(String year) {
        mYear = year;
    }

    public String getFileName() {
        return fileName;
    }
}
