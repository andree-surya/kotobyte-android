package com.kotobyte.searchpage;

import com.kotobyte.base.DataRepository;
import com.kotobyte.models.Kanji;
import com.kotobyte.models.Literal;
import com.kotobyte.models.Word;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class SearchPagePresenter implements SearchPageContracts.Presenter {

    private SearchPageContracts.View mView;
    private DataRepository mDataRepository;
    private String mSearchQuery;

    private Scheduler mBackgroundScheduler = Schedulers.io();
    private Scheduler mMainThreadScheduler = AndroidSchedulers.mainThread();
    private Disposable mWordSearchOperationSubscription;
    private Disposable mKanjiSearchOperationSubscription;

    SearchPagePresenter(SearchPageContracts.View view, DataRepository dataRepository, String searchQuery) {
        mView = view;

        mDataRepository = dataRepository;
        mSearchQuery = searchQuery;
    }

    @Override
    public void onCreate() {
        searchWords();
    }

    @Override
    public void onDestroy() {

        if (mWordSearchOperationSubscription != null) {
            mWordSearchOperationSubscription.dispose();
        }

        if (mKanjiSearchOperationSubscription != null) {
            mKanjiSearchOperationSubscription.dispose();
        }
    }

    @Override
    public void onRequestKanjiListForWord(int position, Word word) {
        searchKanjiForWord(position, word);
    }

    @Override
    public void onRequestDetailForKanji(Kanji kanji) {
        mView.showKanjiDetailScreen(kanji);
    }

    private void searchWords() {

        mView.showWordSearchProgressBar(true);
        mView.showNoWordSearchResultsLabel(false);
        mView.showWordSearchResultsView(false);

        mWordSearchOperationSubscription = mDataRepository.searchWords(mSearchQuery)
                .subscribeOn(mBackgroundScheduler)
                .observeOn(mMainThreadScheduler)
                .subscribe(new Consumer<List<Word>>() {

                    @Override
                    public void accept(@NonNull List<Word> words) throws Exception {

                        mView.showWordSearchProgressBar(false);

                        if (words.isEmpty()) {
                            mView.showNoWordSearchResultsLabel(true);

                        } else {
                            mView.showWordSearchResults(words);
                            mView.showWordSearchResultsView(true);
                        }

                    }

                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {

                        mView.showWordSearchProgressBar(false);
                        mView.showUnknownError(throwable);
                    }
                });
    }

    private void searchKanjiForWord(final int position, final Word word) {

        if (mKanjiSearchOperationSubscription != null) {
            mKanjiSearchOperationSubscription.dispose();
        }

        mKanjiSearchOperationSubscription = mDataRepository.searchKanji(getKanjiSearchQueryForWord(word))
                .subscribeOn(mBackgroundScheduler)
                .observeOn(mMainThreadScheduler)
                .delaySubscription(100, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<List<Kanji>>() {

                    @Override
                    public void accept(@NonNull List<Kanji> kanjiList) throws Exception {

                        mView.showKanjiSearchResults(position, kanjiList);
                    }

                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {

                        mView.showUnknownError(throwable);
                    }
                });
    }

    private String getKanjiSearchQueryForWord(Word word) {
        StringBuilder queryBuilder = new StringBuilder();

        for (Literal literal : word.getLiterals()) {
            queryBuilder.append(literal.getText());
        }

        return queryBuilder.toString();
    }
}