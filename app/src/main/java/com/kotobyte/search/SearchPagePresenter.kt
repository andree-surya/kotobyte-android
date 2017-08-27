package com.kotobyte.search

import com.kotobyte.models.Kanji
import com.kotobyte.models.Word
import com.kotobyte.utils.AsynchronousTask

internal class SearchPagePresenter(
        private val view: SearchPageContracts.View,
        private val dataSource: SearchPageContracts.DataSource,
        private val searchQuery: String

) : SearchPageContracts.Presenter {

    private var searchWordsTask: AsynchronousTask<*>? = null
    private var searchKanjiTask: AsynchronousTask<*>? = null

    override fun onCreate() {
        searchWords()
    }

    override fun onDestroy() {
        searchWordsTask?.cancel(true)
        searchKanjiTask?.cancel(true)
    }

    override fun onRequestKanjiListForWord(position: Int, word: Word) {
        searchKanjiForWord(position, word)
    }

    override fun onRequestDetailForKanji(kanji: Kanji) {
        view.showKanjiDetailScreen(kanji)
    }

    private fun searchWords() {
        searchWordsTask = SearchWordsTask(searchQuery).apply { execute() }
    }

    private fun searchKanjiForWord(position: Int, word: Word) {
        searchKanjiTask = SearchKanjiTask(position, word).apply { execute() }
    }

    private inner class SearchWordsTask(val query: String) : AsynchronousTask<List<Word>>() {

        override fun doInBackground(): List<Word> = dataSource.searchWords(query)

        override fun onPreExecute() {

            view.showWordSearchProgressBar(true)
            view.showNoWordSearchResultsLabel(false)
            view.showWordSearchResultsView(false)
        }

        override fun onPostExecute(data: List<Word>?, error: Throwable?) {
            view.showWordSearchProgressBar(false)

            if (error == null) {

                if (data != null && data.isNotEmpty()) {
                    view.showWordSearchResults(data)
                    view.showWordSearchResultsView(true)

                } else {
                    view.showNoWordSearchResultsLabel(true)
                }

            } else {
                view.showUnknownError(error)
            }
        }
    }

    private inner class SearchKanjiTask(val position: Int, val word: Word) : AsynchronousTask<List<Kanji>>() {

        override fun doInBackground(): List<Kanji> {

            val queryBuilder = StringBuilder()

            for ((text) in word.literals) {
                queryBuilder.append(text)
            }

            return dataSource.searchKanji(queryBuilder.toString())
        }

        override fun onPostExecute(data: List<Kanji>?, error: Throwable?) {

            if (error == null) {

                if (data != null) {
                    view.showKanjiSearchResults(position, data)
                }

            } else {
                view.showUnknownError(error)
            }
        }
    }
}
