package com.kotobyte.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/02.
 */
public class SearchResults {

    @SerializedName("words")
    private List<Word> mWords;

    public Word getWord(int index) {
        return mWords.get(index);
    }

    public int getSize() {
        return mWords.size();
    }
}
