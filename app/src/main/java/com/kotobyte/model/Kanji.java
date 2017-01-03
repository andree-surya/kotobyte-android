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

    int getID() {
        return mID;
    }

    String getLiteral() {
        return mLiteral;
    }

    List<String> getReadings() {
        return mReadings;
    }

    List<String> getMeanings() {
        return mMeanings;
    }

    List<String> getExtras() {
        return mExtras;
    }

    List<String> getStrokes() {
        return mStrokes;
    }
}
