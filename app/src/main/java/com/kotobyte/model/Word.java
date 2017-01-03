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

    private transient String mSensesDisplayText;

    public int getID () {
        return mID;
    }

    public List<Literal> getLiterals() {
        return mLiterals;
    }

    public List<Literal> getReadings() {
        return mReadings;
    }

    public List<Sense> getSenses() {
        return mSenses;
    }
}
