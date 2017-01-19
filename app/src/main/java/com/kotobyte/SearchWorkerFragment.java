package com.kotobyte;

import android.support.v4.app.Fragment;
import android.util.SparseArray;

import com.kotobyte.model.KanjiSearchResults;
import com.kotobyte.model.Literal;
import com.kotobyte.model.WordSearchResults;
import com.kotobyte.util.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by andree.surya on 2017/01/01.
 */
public class SearchWorkerFragment extends Fragment {

    public static final String TAG = SearchWorkerFragment.class.getSimpleName();

    private WordSearchResults mWordSearchResults;
    private SparseArray<KanjiSearchResults> mKanjiSearchResults;

    private Listener mListener;

    public SearchWorkerFragment() {
        setRetainInstance(true);
    }

    void setListener(Listener listener) {
        mListener = listener;
    }

    void searchWords(final String query) {

        if (mWordSearchResults == null) {

            WebService.Interface webService = WebService.getInstance(getContext()).getInterface();

            webService.searchWords(query).enqueue(new Callback<WordSearchResults>() {

                @Override
                public void onResponse(Call<WordSearchResults> call, Response<WordSearchResults> response) {

                    if (response.isSuccessful()) {
                        mWordSearchResults = response.body();
                        mKanjiSearchResults = new SparseArray<>(mWordSearchResults.getSize());

                        mListener.onReceiveWordSearchResults(mWordSearchResults);

                    } else {
                        mListener.onReceiveErrorWhileSearchingForWords(WebService.Error.UNKNOWN);
                    }
                }

                @Override
                public void onFailure(Call<WordSearchResults> call, Throwable t) {
                    mListener.onReceiveErrorWhileSearchingForWords(WebService.Error.NETWORK);
                }
            });

        } else {
            mListener.onReceiveWordSearchResults(mWordSearchResults);
        }
    }

    void searchKanji(final int position) {

        KanjiSearchResults kanjiSearchResults = mKanjiSearchResults.get(position);

        if (kanjiSearchResults == null) {

            StringBuilder queryBuilder = new StringBuilder();

            for (Literal literal : mWordSearchResults.getWord(position).getLiterals()) {
                queryBuilder.append(literal.getText());
            }

            WebService.Interface webService = WebService.getInstance(getContext()).getInterface();

            webService.searchKanji(queryBuilder.toString()).enqueue(new Callback<KanjiSearchResults>() {

                @Override
                public void onResponse(Call<KanjiSearchResults> call, Response<KanjiSearchResults> response) {

                    if (response.isSuccessful()) {
                        mKanjiSearchResults.setValueAt(position, response.body());
                        mListener.onReceiveKanjiSearchResults(response.body(), position);

                    } else {
                        mListener.onReceiveErrorWhileSearchingForKanji(WebService.Error.UNKNOWN, position);
                    }
                }

                @Override
                public void onFailure(Call<KanjiSearchResults> call, Throwable t) {
                    mListener.onReceiveErrorWhileSearchingForKanji(WebService.Error.NETWORK, position);
                }
            });

        } else {
            mListener.onReceiveKanjiSearchResults(kanjiSearchResults, position);
        }
    }

    interface Listener {
        void onReceiveWordSearchResults(WordSearchResults wordSearchResults);
        void onReceiveKanjiSearchResults(KanjiSearchResults kanjiSearchResults, int position);

        void onReceiveErrorWhileSearchingForWords(WebService.Error error);
        void onReceiveErrorWhileSearchingForKanji(WebService.Error error, int position);
    }
}
