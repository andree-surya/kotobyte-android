package com.kotobyte.searchpage;

import android.util.Log;

import com.kotobyte.models.db.DictionaryDatabase;
import com.kotobyte.models.Kanji;
import com.kotobyte.models.Word;

import java.io.File;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;

class SearchPageDataSource implements SearchPageContracts.DataSource {
    private static final String TAG = SearchPageDataSource.class.getSimpleName();

    private File mDictionaryFilePath;
    private DictionaryDatabase mDictionaryDatabase;

    SearchPageDataSource(File dictionaryFilePath) {
        mDictionaryFilePath = dictionaryFilePath;
    }

    @Override
    public Single<List<Word>> searchWords(final String query) {

        return Single.create(new SingleOnSubscribe<List<Word>>() {

            @Override
            public void subscribe(@NonNull SingleEmitter<List<Word>> e) throws Exception {

                prepareDictionaryDatabase();

                e.onSuccess(mDictionaryDatabase.searchWord(query));
            }
        });
    }

    @Override
    public Single<List<Kanji>> searchKanji(final String query) {

        return Single.create(new SingleOnSubscribe<List<Kanji>>() {

            @Override
            public void subscribe(@NonNull SingleEmitter<List<Kanji>> e) throws Exception {

                prepareDictionaryDatabase();

                e.onSuccess(mDictionaryDatabase.searchKanji(query));
            }
        });
    }

    void close() {

        try {
            if (mDictionaryDatabase != null) {
                mDictionaryDatabase.close();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    private void prepareDictionaryDatabase() {

        if (mDictionaryDatabase == null) {
            mDictionaryDatabase = new DictionaryDatabase(mDictionaryFilePath.getAbsolutePath());
        }
    }
}
