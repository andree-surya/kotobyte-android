package com.kotobyte.util;

import android.content.Context;

import com.kotobyte.model.Kanji;
import com.kotobyte.model.SearchResults;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
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

        @GET("search.json")
        Call<SearchResults> searchWords(@Query("query") String query);

        @GET("kanji/{literal}.json")
        Call<Kanji> getKanji(@Path("literal") String literal);
    }
}
