package com.activeminds.projectrepo.models;

public class IntroScreenModel {
    String mTitle;
    String mDescription;
    int mImage;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public int getImage() {
        return mImage;
    }

    public void setImage(int image) {
        mImage = image;
    }

    public IntroScreenModel(String title, String description, int image) {
        mTitle = title;
        mDescription = description;
        mImage = image;
    }
}
