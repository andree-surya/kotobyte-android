package com.kotobyte.models;

import java.util.ArrayList;
import java.util.List;


public class Word {

    private long mID;
    private WordLiteral[] mLiterals;
    private WordLiteral[] mReadings;
    private WordSense[] mSenses;

    public long getID () {
        return mID;
    }

    public WordLiteral[] getLiterals() {
        return mLiterals;
    }

    public WordLiteral[] getReadings() {
        return mReadings;
    }

    public WordSense[] getSenses() {
        return mSenses;
    }

    public static class Builder {

        private long mID;
        private List<WordLiteral> mReadings = new ArrayList<>();
        private List<WordLiteral> mLiterals = new ArrayList<>();
        private List<WordSense> mSenses = new ArrayList<>();

        public void setID(long ID) {
            mID = ID;
        }

        public void addReading(WordLiteral reading) {
            mReadings.add(reading);
        }

        public void addLiteral(WordLiteral wordLiteral) {
            mLiterals.add(wordLiteral);
        }

        public void addSense(WordSense wordSense) {
            mSenses.add(wordSense);
        }

        public Word buildAndReset() {
            Word word = new Word();

            word.mID = mID;
            word.mLiterals = mLiterals.toArray(new WordLiteral[mLiterals.size()]);
            word.mReadings = mReadings.toArray(new WordLiteral[mReadings.size()]);
            word.mSenses = mSenses.toArray(new WordSense[mSenses.size()]);

            mID = 0;
            mLiterals.clear();
            mReadings.clear();
            mSenses.clear();

            return word;
        }
    }
}
