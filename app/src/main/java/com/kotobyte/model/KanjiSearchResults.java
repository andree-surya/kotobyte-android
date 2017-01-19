package com.kotobyte.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/02.
 */
public class KanjiSearchResults {

    @SerializedName("kanji_list")
    private List<Kanji> mKanjiList;

    public List<Kanji> getKanjiList() {
        return mKanjiList;
    }

    public Kanji getKanji(int position) {
        return mKanjiList.get(position);
    }

    public int getSize() {
        return mKanjiList.size();
    }
}
