package com.kotobyte.search

import com.kotobyte.models.Kanji
import com.kotobyte.models.Word

internal object SearchPageContracts {

    interface View {

        fun showWordSearchProgressBar(show: Boolean)
        fun showWordSearchResultsView(show: Boolean)
        fun showWordSearchResults(words: List<Word>)
        fun showNoWordSearchResultsLabel(show: Boolean)
        fun showKanjiSearchResults(position: Int, kanjiList: List<Kanji>)
        fun showKanjiDetailScreen(kanji: Kanji)
        fun showUnknownError(error: Throwable)
    }

    interface Presenter {

        fun onCreate()
        fun onDestroy()

        fun onRequestKanjiListForWord(position: Int, word: Word)
        fun onRequestDetailForKanji(kanji: Kanji)
    }

    interface DataSource {
        fun searchWords(query: String): List<Word>
        fun searchKanji(query: String): List<Kanji>
    }
}
