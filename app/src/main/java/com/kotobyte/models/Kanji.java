package com.kotobyte.models;

import android.os.Parcel;
import android.os.Parcelable;


public class Kanji implements Parcelable {

    private long mID;
    private String mLiteral;
    private String[] mReadings;
    private String[] mMeanings;
    private String[] mExtras;
    private String[] mStrokes;

    public long getID() {
        return mID;
    }

    public String getLiteral() {
        return mLiteral;
    }

    public String[] getReadings() {
        return mReadings;
    }

    public String[] getMeanings() {
        return mMeanings;
    }

    public String[] getExtras() {
        return mExtras;
    }

    public String[] getStrokes() {
        return mStrokes;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLong(mID);
        dest.writeString(mLiteral);
        dest.writeStringArray(mReadings);
        dest.writeStringArray(mMeanings);
        dest.writeStringArray(mExtras);
        dest.writeStringArray(mStrokes);
    }

    public static final Creator<Kanji> CREATOR = new Creator<Kanji>() {

        @Override
        public Kanji createFromParcel(Parcel in) {
            Kanji kanji = new Kanji();

            kanji.mID = in.readLong();
            kanji.mLiteral = in.readString();
            kanji.mReadings = in.createStringArray();
            kanji.mMeanings = in.createStringArray();
            kanji.mExtras = in.createStringArray();
            kanji.mStrokes = in.createStringArray();

            return kanji;
        }

        @Override
        public Kanji[] newArray(int size) {
            return new Kanji[size];
        }
    };

    public static class Builder {

        private long mID;
        private String mLiteral;
        private String[] mReadings;
        private String[] mMeanings;
        private String[] mExtras;
        private String[] mStrokes;

        public void setID(long ID) {
            mID = ID;
        }

        public void setLiteral(String literal) {
            mLiteral = literal;
        }

        public void setReadings(String[] readings) {
            mReadings = readings;
        }

        public void setMeanings(String[] meanings) {
            mMeanings = meanings;
        }

        public void setExtras(String[] extras) {
            mExtras = extras;
        }

        public void setStrokes(String[] strokes) {
            mStrokes = strokes;
        }

        public Kanji buildAndReset() {
            Kanji kanji = new Kanji();

            kanji.mID = mID;
            kanji.mLiteral = mLiteral;
            kanji.mReadings = mReadings;
            kanji.mMeanings = mMeanings;
            kanji.mExtras = mExtras;
            kanji.mStrokes = mStrokes;

            mID = 0;
            mLiteral = null;
            mReadings = null;
            mMeanings = null;
            mExtras = null;
            mReadings = null;

            return kanji;
        }
    }
}
