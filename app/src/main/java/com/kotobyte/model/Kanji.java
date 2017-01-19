package com.kotobyte.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/02.
 */
public class Kanji {

    @SerializedName("id")
    private int mID;

    @SerializedName("literal")
    private String mLiteral;

    @SerializedName("readings")
    private List<String> mReadings;

    @SerializedName("meanings")
    private List<String> mMeanings;

    @SerializedName("extras")
    private List<String> mExtras;

    @SerializedName("strokes")
    private List<String> mStrokes;

    public int getID() {
        return mID;
    }

    public String getLiteral() {
        return mLiteral;
    }

    public List<String> getReadings() {
        return mReadings;
    }

    public List<String> getMeanings() {
        return mMeanings;
    }

    public List<String> getExtras() {
        return mExtras;
    }

    public List<String> getStrokes() {
        return mStrokes;
    }
}
