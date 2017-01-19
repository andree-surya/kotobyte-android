package com.kotobyte.util;

import android.content.Context;

import com.kotobyte.model.KanjiSearchResults;
import com.kotobyte.model.WordSearchResults;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by andree.surya on 2017/01/02.
 */
public class WebService {
    private static WebService sInstance;

    private Interface mInterface;

    private WebService(String webServiceURL) {

        mInterface = new Retrofit.Builder()
                .baseUrl(webServiceURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WebService.Interface.class);
    }

    public static synchronized WebService getInstance(Context context) {

        if (sInstance == null) {

            AppConfig appConfig = AppConfig.getInstance(context);
            String webServiceURL = appConfig.getWebServiceURL();

            sInstance = new WebService(webServiceURL);
        }

        return sInstance;
    }

    public Interface getInterface() {
        return mInterface;
    }

    public enum Error {
        UNKNOWN,
        NETWORK,
        NOT_FOUND
    }

    public interface Interface {

        @GET("words.json")
        Call<WordSearchResults> searchWords(@Query("query") String query);

        @GET("kanji.json")
        Call<KanjiSearchResults> searchKanji(@Query("query") String query);
    }
}
