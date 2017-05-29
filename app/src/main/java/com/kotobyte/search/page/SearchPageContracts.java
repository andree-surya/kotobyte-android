package com.kotobyte.search.page;

import com.kotobyte.models.Kanji;
import com.kotobyte.models.Word;

import java.util.List;

interface SearchPageContracts {

    interface View {

        void showWordSearchProgressBar(boolean show);
        void showWordSearchResultsView(boolean show);
        void showWordSearchResults(List<Word> words);
        void showNoWordSearchResultsLabel(boolean show);
        void showKanjiSearchResults(int position, List<Kanji> kanjiList);
        void showKanjiDetailScreen(Kanji kanji);
        void showUnknownError(Throwable error);
    }

    interface Presenter {

        void onCreate();
        void onDestroy();

        void onRequestKanjiListForWord(int position, Word word);
        void onRequestDetailForKanji(Kanji kanji);
    }
}
