package com.kotobyte.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sense {

    private String[] mTexts;
    private String[] mCategories;
    private String[] mLabels;
    private String[] mNotes;
    private Origin[] mOrigins;

    public String[] getTexts() {
        return mTexts;
    }

    public String[] getCategories() {
        return mCategories;
    }

    public String[] getLabels() {
        return mLabels;
    }

    public String[] getNotes() {
        return mNotes;
    }

    public Origin[] getOrigins() {
        return mOrigins;
    }

    public static class Builder {

        private List<String> mTexts = new ArrayList<>();
        private List<String> mCategories = new ArrayList<>();
        private List<String> mLabels = new ArrayList<>();
        private List<String> mNotes = new ArrayList<>();
        private List<Origin> mOrigins = new ArrayList<>();

        public void addTexts(String[] texts) {
            Collections.addAll(mTexts, texts);
        }

        public void addCategories(String[] categories) {
            Collections.addAll(mCategories, categories);
        }

        public void addLabels(String[] labels) {
            Collections.addAll(mLabels, labels);
        }

        public void addNotes(String[] notes) {
            Collections.addAll(mNotes, notes);
        }

        public void addOrigin(Origin origin) {
            mOrigins.add(origin);
        }

        public Sense buildAndReset() {
            Sense sense = new Sense();

            sense.mTexts = mTexts.toArray(new String[mTexts.size()]);
            sense.mCategories = mCategories.toArray(new String[mCategories.size()]);
            sense.mLabels = mLabels.toArray(new String[mLabels.size()]);
            sense.mNotes = mNotes.toArray(new String[mNotes.size()]);
            sense.mOrigins = mOrigins.toArray(new Origin[mOrigins.size()]);

            mTexts.clear();
            mCategories.clear();
            mLabels.clear();
            mNotes.clear();
            mOrigins.clear();;

            return sense;
        }
    }
}
