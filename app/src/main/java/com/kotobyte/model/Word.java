package com.kotobyte.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Word {

    @SerializedName("id")
    private int mID;

    @SerializedName("literals")
    private List<Literal> mLiterals;

    @SerializedName("readings")
    private List<Literal> mReadings;

    @SerializedName("senses")
    private List<Sense> mSenses;

    int getID () {
        return mID;
    }

    List<Literal> getLiterals() {
        return mLiterals;
    }

    List<Literal> getReadings() {
        return mReadings;
    }

    List<Sense> getSenses() {
        return mSenses;
    }
}
