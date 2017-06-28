package com.kotobyte.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Kanji implements Parcelable {

    private long mId;
    private String mCharacter;
    private String[] mReadings;
    private String[] mMeanings;
    private String[] mStrokes;
    private String[] mExtras;

    public long getId() {
        return mId;
    }

    public String getCharacter() {
        return mCharacter;
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

    public String[] getExtras() {
        return mExtras;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLong(mId);
        dest.writeString(mCharacter);
        dest.writeStringArray(mReadings);
        dest.writeStringArray(mMeanings);
        dest.writeStringArray(mStrokes);
        dest.writeStringArray(mExtras);
    }

    public static final Creator<Kanji> CREATOR = new Creator<Kanji>() {

        @Override
        public Kanji createFromParcel(Parcel in) {
            Kanji kanji = new Kanji();

            kanji.mId = in.readLong();
            kanji.mCharacter = in.readString();
            kanji.mReadings = in.createStringArray();
            kanji.mMeanings = in.createStringArray();
            kanji.mStrokes = in.createStringArray();
            kanji.mExtras = in.createStringArray();

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
        private List<String> mReadings = new ArrayList<>();
        private List<String> mMeanings = new ArrayList<>();
        private List<String> mStrokes = new ArrayList<>();
        private List<String> mExtras = new ArrayList<>();

        public void setId(long id) {
            mId = id;
        }

        public void setCharacter(String character) {
            mCharacter = character;
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

        public void addExtra(String extra) {
            mExtras.add(extra);
        }

        public Kanji build() {
            Kanji kanji = new Kanji();

            kanji.mId = mId;
            kanji.mCharacter = mCharacter;
            kanji.mReadings = mReadings.toArray(new String[mReadings.size()]);
            kanji.mMeanings = mMeanings.toArray(new String[mMeanings.size()]);
            kanji.mStrokes = mStrokes.toArray(new String[mStrokes.size()]);
            kanji.mExtras = mExtras.toArray(new String[mExtras.size()]);

            return kanji;
        }

        public void reset() {

            mId = 0;
            mCharacter = null;
            mReadings.clear();
            mMeanings.clear();
            mReadings.clear();
            mStrokes.clear();
            mExtras.clear();
        }
    }
}
