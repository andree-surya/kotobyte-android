package com.kotobyte.search;

import com.kotobyte.base.DatabaseProvider;
import com.kotobyte.models.Kanji;
import com.kotobyte.models.Word;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;

class SearchPageDataSource implements SearchPageContracts.DataSource {

    private DatabaseProvider mDatabaseProvider;

    SearchPageDataSource(DatabaseProvider databaseProvider) {
        mDatabaseProvider = databaseProvider;
    }

    @Override
    public Single<List<Word>> searchWords(final String query) {
        return Single.fromCallable(new SearchWords(mDatabaseProvider, query));
    }

    @Override
    public Single<List<Kanji>> searchKanji(final String query) {
        return Single.fromCallable(new SearchKanji(mDatabaseProvider, query));
    }

    private static class SearchWords implements Callable<List<Word>> {

        private DatabaseProvider mDatabaseProvider;
        private String mQuery;

        SearchWords(DatabaseProvider databaseProvider, String query) {
            mDatabaseProvider = databaseProvider;
            mQuery = query;
        }

        @Override
        public List<Word> call() throws Exception {
            return mDatabaseProvider.getConnection().searchWords(mQuery);
        }
    }

    private static class SearchKanji implements Callable<List<Kanji>> {

        private DatabaseProvider mDatabaseProvider;
        private String mQuery;

        SearchKanji(DatabaseProvider databaseProvider, String query) {
            mDatabaseProvider = databaseProvider;
            mQuery = query;
        }

        @Override
        public List<Kanji> call() throws Exception {
            return mDatabaseProvider.getConnection().searchKanji(mQuery);
        }
    }
}
