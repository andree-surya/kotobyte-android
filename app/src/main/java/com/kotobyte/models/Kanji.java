package com.kotobyte.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Kanji implements Parcelable {

    @SerializedName("id")
    private long mID;

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

    private Kanji(Parcel in) {
        mID = in.readLong();
        mLiteral = in.readString();
        mReadings = in.createStringArrayList();
        mMeanings = in.createStringArrayList();
        mExtras = in.createStringArrayList();
        mStrokes = in.createStringArrayList();
    }

    public long getID() {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mID);
        dest.writeString(mLiteral);
        dest.writeStringList(mReadings);
        dest.writeStringList(mMeanings);
        dest.writeStringList(mExtras);
        dest.writeStringList(mStrokes);
    }

    public static final Creator<Kanji> CREATOR = new Creator<Kanji>() {

        @Override
        public Kanji createFromParcel(Parcel in) {
            return new Kanji(in);
        }

        @Override
        public Kanji[] newArray(int size) {
            return new Kanji[size];
        }
    };
}
