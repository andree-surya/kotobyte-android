package com.kotobyte.models;

import java.util.ArrayList;
import java.util.List;


public class Word {

    private long mID;
    private Literal[] mLiterals;
    private Literal[] mReadings;
    private Sense[] mSenses;

    public long getID () {
        return mID;
    }

    public Literal[] getLiterals() {
        return mLiterals;
    }

    public Literal[] getReadings() {
        return mReadings;
    }

    public Sense[] getSenses() {
        return mSenses;
    }

    public static class Builder {

        private long mID;
        private List<Literal> mReadings = new ArrayList<>();
        private List<Literal> mLiterals = new ArrayList<>();
        private List<Sense> mSenses = new ArrayList<>();

        public void setID(long ID) {
            mID = ID;
        }

        public void addReading(Literal reading) {
            mReadings.add(reading);
        }

        public void addLiteral(Literal wordLiteral) {
            mLiterals.add(wordLiteral);
        }

        public void addSense(Sense sense) {
            mSenses.add(sense);
        }

        public Word buildAndReset() {
            Word word = new Word();

            word.mID = mID;
            word.mLiterals = mLiterals.toArray(new Literal[mLiterals.size()]);
            word.mReadings = mReadings.toArray(new Literal[mReadings.size()]);
            word.mSenses = mSenses.toArray(new Sense[mSenses.size()]);

            mID = 0;
            mLiterals.clear();
            mReadings.clear();
            mSenses.clear();

            return word;
        }
    }
}
