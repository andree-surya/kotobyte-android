package com.kotobyte.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Kanji implements Parcelable {

    private long mId;
    private String mCharacter;
    private short mJlpt;
    private short mGrade;
    private String[] mReadings;
    private String[] mMeanings;
    private String[] mStrokes;

    public long getId() {
        return mId;
    }

    public String getCharacter() {
        return mCharacter;
    }

    public short getJlpt() {
        return mJlpt;
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

        dest.writeLong(mId);
        dest.writeString(mCharacter);
        dest.writeInt(mJlpt);
        dest.writeInt(mGrade);
        dest.writeStringArray(mReadings);
        dest.writeStringArray(mMeanings);
        dest.writeStringArray(mStrokes);
    }

    public static final Creator<Kanji> CREATOR = new Creator<Kanji>() {

        @Override
        public Kanji createFromParcel(Parcel in) {
            Kanji kanji = new Kanji();

            kanji.mId = in.readLong();
            kanji.mCharacter = in.readString();
            kanji.mJlpt = (short) in.readInt();
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

        private long mId;
        private String mCharacter;
        private short mJlpt;
        private short mGrade;
        private List<String> mReadings = new ArrayList<>();
        private List<String> mMeanings = new ArrayList<>();
        private List<String> mStrokes = new ArrayList<>();

        public void setId(long id) {
            mId = id;
        }

        public void setCharacter(String character) {
            mCharacter = character;
        }

        public void setJlpt(short jlpt) {
            mJlpt = jlpt;
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

        public Kanji build() {
            Kanji kanji = new Kanji();

            kanji.mId = mId;
            kanji.mCharacter = mCharacter;
            kanji.mJlpt = mJlpt;
            kanji.mGrade = mGrade;
            kanji.mReadings = mReadings.toArray(new String[mReadings.size()]);
            kanji.mMeanings = mMeanings.toArray(new String[mMeanings.size()]);
            kanji.mStrokes = mStrokes.toArray(new String[mStrokes.size()]);

            return kanji;
        }

        public void reset() {

            mId = 0;
            mCharacter = null;
            mJlpt = 0;
            mGrade = 0;
            mReadings.clear();
            mMeanings.clear();
            mReadings.clear();
            mStrokes.clear();
        }
    }
}
