package com.kotobyte.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordSense {

    private String[] mTexts;
    private String[] mCategories;
    private String[] mLabels;
    private String[] mNotes;
    private WordOrigin[] mOrigins;

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

    public WordOrigin[] getOrigins() {
        return mOrigins;
    }

    public static class Builder {

        private List<String> mTexts = new ArrayList<>();
        private List<String> mCategories = new ArrayList<>();
        private List<String> mLabels = new ArrayList<>();
        private List<String> mNotes = new ArrayList<>();
        private List<WordOrigin> mOrigins = new ArrayList<>();

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

        public void addOrigin(WordOrigin origin) {
            mOrigins.add(origin);
        }

        public WordSense buildAndReset() {
            WordSense wordSense = new WordSense();

            wordSense.mTexts = mTexts.toArray(new String[mTexts.size()]);
            wordSense.mCategories = mCategories.toArray(new String[mCategories.size()]);
            wordSense.mLabels = mLabels.toArray(new String[mLabels.size()]);
            wordSense.mNotes = mNotes.toArray(new String[mNotes.size()]);
            wordSense.mOrigins = mOrigins.toArray(new WordOrigin[mOrigins.size()]);

            mTexts.clear();
            mCategories.clear();
            mLabels.clear();
            mNotes.clear();
            mOrigins.clear();;

            return wordSense;
        }
    }
}
