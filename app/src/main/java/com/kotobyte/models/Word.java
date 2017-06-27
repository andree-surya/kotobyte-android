package com.kotobyte.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Word {

    private long mId;
    private Literal[] mLiterals;
    private Literal[] mReadings;
    private Sense[] mSenses;

    public long getId() {
        return mId;
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

        private long mId;
        private List<Literal> mReadings = new ArrayList<>();
        private List<Literal> mLiterals = new ArrayList<>();
        private List<Sense> mSenses = new ArrayList<>();

        public void setId(long id) {
            mId = id;
        }

        public void addLiterals(Literal[] literals) {
            Collections.addAll(mLiterals, literals);
        }

        public void addReadings(Literal[] readings) {
            Collections.addAll(mReadings, readings);
        }

        public void addSenses(Sense[] senses) {
            Collections.addAll(mSenses, senses);
        }

        public Word build() {
            Word word = new Word();

            word.mId = mId;
            word.mLiterals = mLiterals.toArray(new Literal[mLiterals.size()]);
            word.mReadings = mReadings.toArray(new Literal[mReadings.size()]);
            word.mSenses = mSenses.toArray(new Sense[mSenses.size()]);

            return word;
        }

        public void reset() {

            mId = 0;
            mLiterals.clear();
            mReadings.clear();
            mSenses.clear();
        }
    }
}
