package com.kotobyte.models;

public class Sense {

    private String[] mTexts;
    private String[] mCategories;
    private String[] mExtras;

    public String[] getTexts() {
        return mTexts;
    }

    public String[] getCategories() {
        return mCategories;
    }

    public String[] getExtras() {
        return mExtras;
    }

    public static class Builder {

        private String[] mTexts;
        private String[] mCategories;
        private String[] mExtras;

        public void setTexts(String[] texts) {
            mTexts = texts;
        }

        public void setCategories(String[] categories) {
            mCategories = categories;
        }

        public void setExtras(String[] extras) {
            mExtras = extras;
        }

        public Sense buildAndReset() {
            Sense sense = new Sense();

            sense.mTexts = mTexts;
            sense.mCategories = mCategories;
            sense.mExtras = mExtras;

            mTexts = null;
            mCategories = null;
            mExtras = null;

            return sense;
        }
    }
}
