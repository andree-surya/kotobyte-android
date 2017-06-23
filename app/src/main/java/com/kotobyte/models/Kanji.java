package com.kotobyte.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Kanji implements Parcelable {

    private long mID;
    private String mLiteral;
    private short mJLPT;
    private short mGrade;
    private String[] mReadings;
    private String[] mMeanings;
    private String[] mStrokes;

    public long getID() {
        return mID;
    }

    public String getLiteral() {
        return mLiteral;
    }

    public short getJLPT() {
        return mJLPT;
    }

    public short getGrade() {
        return mGrade;
    }

    public String[] getReadings() {
        return mReadings;
    }

    public String[] getMeanings() {
        return mMeanings;
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
        dest.writeInt(mJLPT);
        dest.writeInt(mGrade);
        dest.writeStringArray(mReadings);
        dest.writeStringArray(mMeanings);
        dest.writeStringArray(mStrokes);
    }

    public static final Creator<Kanji> CREATOR = new Creator<Kanji>() {

        @Override
        public Kanji createFromParcel(Parcel in) {
            Kanji kanji = new Kanji();

            kanji.mID = in.readLong();
            kanji.mLiteral = in.readString();
            kanji.mJLPT = (short) in.readInt();
            kanji.mGrade = (short) in.readInt();
            kanji.mReadings = in.createStringArray();
            kanji.mMeanings = in.createStringArray();
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
        private short mJLPT;
        private short mGrade;
        private List<String> mReadings = new ArrayList<>();
        private List<String> mMeanings = new ArrayList<>();
        private List<String> mStrokes = new ArrayList<>();

        public void setID(long ID) {
            mID = ID;
        }

        public void setLiteral(String literal) {
            mLiteral = literal;
        }

        public void setJLPT(short JLPT) {
            mJLPT = JLPT;
        }

        public void setGrade(short grade) {
            mGrade = grade;
        }

        public void addReadings(String[] readings) {
            Collections.addAll(mReadings, readings);
        }

        public void addMeanings(String[] meanings) {
            Collections.addAll(mMeanings, meanings);
        }

        public void addStrokes(String[] strokes) {
            Collections.addAll(mStrokes, strokes);
        }

        public Kanji buildAndReset() {
            Kanji kanji = new Kanji();

            kanji.mID = mID;
            kanji.mLiteral = mLiteral;
            kanji.mJLPT = mJLPT;
            kanji.mGrade = mGrade;
            kanji.mReadings = mReadings.toArray(new String[mReadings.size()]);
            kanji.mMeanings = mMeanings.toArray(new String[mMeanings.size()]);
            kanji.mStrokes = mStrokes.toArray(new String[mStrokes.size()]);

            mID = 0;
            mLiteral = null;
            mJLPT = 0;
            mGrade = 0;
            mReadings.clear();
            mMeanings.clear();
            mReadings.clear();

            return kanji;
        }
    }
}
