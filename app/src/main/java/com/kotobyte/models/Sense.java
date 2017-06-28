package com.kotobyte.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sense {

    private String mText;
    private String[] mCategories;
    private String[] mExtras;
    private Origin[] mOrigins;

    public String getText() {
        return mText;
    }

    public String[] getCategories() {
        return mCategories;
    }

    public String[] getExtras() {
        return mExtras;
    }

    public Origin[] getOrigins() {
        return mOrigins;
    }

    public static class Builder {

        private String mText;
        private List<String> mCategories = new ArrayList<>();
        private List<String> mExtras = new ArrayList<>();
        private List<Origin> mOrigins = new ArrayList<>();

        public void setText(String text) {
            mText = text;
        }

        public void addCategories(String[] categories) {
            Collections.addAll(mCategories, categories);
        }

        public void addExtras(String[] extras) {
            Collections.addAll(mExtras, extras);
        }

        public void addOrigins(Origin[] origins) {
            Collections.addAll(mOrigins, origins);
        }

        public Sense build() {
            Sense sense = new Sense();

            sense.mText = mText;
            sense.mCategories = mCategories.toArray(new String[mCategories.size()]);
            sense.mExtras = mExtras.toArray(new String[mExtras.size()]);
            sense.mOrigins = mOrigins.toArray(new Origin[mOrigins.size()]);

            return sense;
        }

        public void reset() {

            mText = null;
            mCategories.clear();
            mExtras.clear();
            mOrigins.clear();
        }
    }
}
