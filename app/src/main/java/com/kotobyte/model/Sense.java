package com.kotobyte.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/02.
 */
public class Sense {

    @SerializedName("text")
    private String mText;

    @SerializedName("categories")
    private List<String> mCategories;

    @SerializedName("extras")
    private List<String> mExtras;

    public String getText() {
        return mText;
    }

    public List<String> getCategories() {
        return mCategories;
    }

    public List<String> getExtras() {
        return mExtras;
    }
}
