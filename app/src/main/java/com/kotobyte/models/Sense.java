package com.kotobyte.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sense {

    private String mText;
    private String[] mCategories;
    private String[] mLabels;
    private String[] mNotes;
    private Origin[] mOrigins;

    public String getText() {
        return mText;
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

        private String mText;
        private List<String> mCategories = new ArrayList<>();
        private List<String> mLabels = new ArrayList<>();
        private List<String> mNotes = new ArrayList<>();
        private List<Origin> mOrigins = new ArrayList<>();

        public void setText(String text) {
            mText = text;
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

        public void addOrigins(Origin[] origins) {
            Collections.addAll(mOrigins, origins);
        }

        public Sense build() {
            Sense sense = new Sense();

            sense.mText = mText;
            sense.mCategories = mCategories.toArray(new String[mCategories.size()]);
            sense.mLabels = mLabels.toArray(new String[mLabels.size()]);
            sense.mNotes = mNotes.toArray(new String[mNotes.size()]);
            sense.mOrigins = mOrigins.toArray(new Origin[mOrigins.size()]);

            return sense;
        }

        public void reset() {

            mText = null;
            mCategories.clear();
            mLabels.clear();
            mNotes.clear();
            mOrigins.clear();
        }
    }
}
