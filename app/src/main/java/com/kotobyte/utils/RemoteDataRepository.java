package com.kotobyte.utils;

import com.google.gson.annotations.SerializedName;
import com.kotobyte.base.DataRepository;
import com.kotobyte.models.Kanji;
import com.kotobyte.models.Word;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RemoteDataRepository implements DataRepository {

    private WebInterface mWebInterface;

    private Map<String, List<Word>> mWordSearchResultsCache;
    private Map<String, List<Kanji>> mKanjiSearchResultsCache;

    public RemoteDataRepository(String webServiceURL) {

        mWebInterface = new Retrofit.Builder()
                .baseUrl(webServiceURL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(WebInterface.class);

        mWordSearchResultsCache = new HashMap<>();
        mKanjiSearchResultsCache = new HashMap<>();
    }

    @Override
    public Single<List<Word>> searchWords(final String query) {

        if (mWordSearchResultsCache.containsKey(query)) {
            return Single.just(mWordSearchResultsCache.get(query));
        }

        return mWebInterface.searchWords(query).map(new Function<WordSearchResults, List<Word>>() {

            @Override
            public List<Word> apply(WordSearchResults wordSearchResults) throws Exception {
                return wordSearchResults.mWords;
            }
        });
    }

    @Override
    public Single<List<Kanji>> searchKanji(final String query) {

        if (mKanjiSearchResultsCache.containsKey(query)) {
            return Single.just(mKanjiSearchResultsCache.get(query));
        }

        return mWebInterface.searchKanji(query).map(new Function<KanjiSearchResults, List<Kanji>>() {

            @Override
            public List<Kanji> apply(KanjiSearchResults kanjiSearchResults) throws Exception {
                return kanjiSearchResults.mKanjiList;
            }
        });
    }

    public void clearCache() {
        mWordSearchResultsCache.clear();
        mKanjiSearchResultsCache.clear();
    }

    private interface WebInterface {

        @GET("words.json")
        Single<WordSearchResults> searchWords(@Query("query") String query);

        @GET("kanji.json")
        Single<KanjiSearchResults> searchKanji(@Query("query") String query);
    }

    private static class WordSearchResults {

        @SerializedName("words")
        private List<Word> mWords;
    }

    private static class KanjiSearchResults {

        @SerializedName("kanji_list")
        private List<Kanji> mKanjiList;
    }
}
