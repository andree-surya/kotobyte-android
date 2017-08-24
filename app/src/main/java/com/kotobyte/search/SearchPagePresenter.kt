package com.kotobyte.search

import com.kotobyte.models.Kanji
import com.kotobyte.models.Word
import java.util.concurrent.TimeUnit

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

internal class SearchPagePresenter(
        private val view: SearchPageContracts.View,
        private val dataSource: SearchPageContracts.DataSource,
        private val searchQuery: String) : SearchPageContracts.Presenter {

    private val backgroundScheduler = Schedulers.io()
    private val mainThreadScheduler = AndroidSchedulers.mainThread()

    private var wordSearchSubscription: Disposable? = null
    private var kanjiSearchSubscription: Disposable? = null

    override fun onCreate() = searchWords()

    override fun onDestroy() {

        wordSearchSubscription?.dispose()
        kanjiSearchSubscription?.dispose()
    }

    override fun onRequestKanjiListForWord(position: Int, word: Word) =
            searchKanjiForWord(position, word)

    override fun onRequestDetailForKanji(kanji: Kanji) = view.showKanjiDetailScreen(kanji)

    private fun searchWords() {

        view.showWordSearchProgressBar(true)
        view.showNoWordSearchResultsLabel(false)
        view.showWordSearchResultsView(false)

        wordSearchSubscription = dataSource.searchWords(searchQuery)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainThreadScheduler)

                .subscribe({ words ->
                    view.showWordSearchProgressBar(false)

                    if (words.isEmpty()) {
                        view.showNoWordSearchResultsLabel(true)

                    } else {
                        view.showWordSearchResults(words)
                        view.showWordSearchResultsView(true)
                    }

                }) { throwable ->
                    view.showWordSearchProgressBar(false)
                    view.showUnknownError(throwable)
                }
    }

    private fun searchKanjiForWord(position: Int, word: Word) {

        kanjiSearchSubscription?.dispose()

        kanjiSearchSubscription = dataSource.searchKanji(getKanjiSearchQueryForWord(word))
                .subscribeOn(backgroundScheduler)
                .observeOn(mainThreadScheduler)
                .delaySubscription<Any>(200, TimeUnit.MILLISECONDS)

                .subscribe({ kanjiList ->
                    view.showKanjiSearchResults(position, kanjiList)

                }) { throwable ->
                    view.showUnknownError(throwable)
                }
    }

    private fun getKanjiSearchQueryForWord(word: Word): String {
        val queryBuilder = StringBuilder()

        for ((text) in word.literals) {
            queryBuilder.append(text)
        }

        return queryBuilder.toString()
    }
}
