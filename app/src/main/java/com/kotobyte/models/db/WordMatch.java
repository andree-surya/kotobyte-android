package com.kotobyte.models.db;

import java.util.Comparator;

class WordMatch {

    private long mId;
    private String mJson;
    private String mHighlights;
    private float mScore;

    WordMatch(long Id, String Json, String highlights, float score) {
        mId = Id;
        mJson = Json;
        mHighlights = highlights;
        mScore = score;
    }

    long getId() {
        return mId;
    }

    String getJson() {
        return mJson;
    }

    String getHighlights() {
        return mHighlights;
    }

    static class ScoreComparator implements Comparator<WordMatch> {

        @Override
        public int compare(WordMatch o1, WordMatch o2) {
            float scoreDiff = o2.mScore - o1.mScore;

            if (scoreDiff > 0) {
                return -1;
            }

            if (scoreDiff < 0) {
                return 1;
            }

            return 0;
        }
    }
}
