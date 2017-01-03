package com.kotobyte;

import android.support.v4.app.Fragment;

import com.kotobyte.model.SearchResults;
import com.kotobyte.util.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by andree.surya on 2017/01/01.
 */
public class SearchWorkerFragment extends Fragment {

    private String mQuery;
    private SearchResults mSearchResults;

    private Listener mListener;

    public SearchWorkerFragment() {
        setRetainInstance(true);
    }

    void setListener(Listener listener) {
        mListener = listener;
    }

    void setQuery(String query) {
        mQuery = query;
        mSearchResults = null;
    }

    void execute() {

        if (mSearchResults == null) {
            mListener.onStartSearching();

            WebService.Interface webService = WebService.getInstance(getContext()).getInterface();

            webService.searchWords(mQuery).enqueue(new Callback<SearchResults>() {

                @Override
                public void onResponse(Call<SearchResults> call, Response<SearchResults> response) {

                    if (response.isSuccessful()) {
                        mSearchResults = response.body();

                        mListener.onFinish(mSearchResults);

                    } else if (response.code() == 404) {
                        mListener.onError(WebService.Error.NOT_FOUND);

                    } else {
                        mListener.onError(WebService.Error.UNKNOWN);
                    }
                }

                @Override
                public void onFailure(Call<SearchResults> call, Throwable t) {
                    mListener.onError(WebService.Error.NETWORK);
                }
            });

        } else {
            mListener.onFinish(mSearchResults);
        }
    }

    interface Listener {
        void onStartSearching();
        void onFinish(SearchResults searchResults);
        void onError(WebService.Error error);
    }
}
