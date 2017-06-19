package com.kotobyte.searchpage;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kotobyte.R;
import com.kotobyte.base.Configuration;
import com.kotobyte.base.ServiceLocator;
import com.kotobyte.databinding.FragmentSearchPageBinding;
import com.kotobyte.models.Kanji;
import com.kotobyte.models.Word;

import java.util.List;

public class SearchPageFragment extends Fragment implements SearchPageContracts.View {

    public static final String TAG = SearchPageFragment.class.getSimpleName();

    private static final String ARG_QUERY = "query";

    private FragmentSearchPageBinding mBinding;
    private SearchPageDataSource mDataSource;
    private SearchPagePresenter mPresenter;
    private SearchResultsAdapter mSearchResultsAdapter;

    public static SearchPageFragment newInstance(CharSequence query) {

        SearchPageFragment fragment = new SearchPageFragment();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_QUERY, query.toString());

        fragment.setArguments(arguments);
        return fragment;
    }

    public String getQuery() {
        return getArguments().getString(ARG_QUERY);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration configuration = ServiceLocator.getInstance().getConfiguration();

        mDataSource = new SearchPageDataSource(configuration.getDictionaryFilePath());
        mPresenter = new SearchPagePresenter(this, mDataSource, getQuery());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_page, container, false);

        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPresenter.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mPresenter.onDestroy();
        mDataSource.close();
    }

    @Override
    public void showWordSearchProgressBar(boolean show) {
        mBinding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showWordSearchResultsView(boolean show) {
        mBinding.searchResultsView.animate().alpha(show ? 1 : 0).start();
    }

    @Override
    public void showWordSearchResults(List<Word> words) {
        mSearchResultsAdapter = new SearchResultsAdapter(getContext(), mWordSearchResultsAdapterListener, words);

        mBinding.searchResultsView.setAdapter(mSearchResultsAdapter);
    }

    @Override
    public void showNoWordSearchResultsLabel(boolean show) {
        mBinding.noSearchResultsLabel.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showKanjiSearchResults(int position, List<Kanji> kanjiList) {
        mSearchResultsAdapter.setKanjiSearchResults(position, kanjiList);
    }

    @Override
    public void showKanjiDetailScreen(Kanji kanji) {
        KanjiDetailDialogFragment.newInstance(kanji).show(getChildFragmentManager(), KanjiDetailDialogFragment.TAG);
    }

    @Override
    public void showUnknownError(Throwable error) {
        Log.e(TAG, error.getLocalizedMessage(), error);

        Toast.makeText(getContext(), R.string.common_unknown_error, Toast.LENGTH_SHORT).show();
    }

    private SearchResultsAdapter.Listener mWordSearchResultsAdapterListener = new SearchResultsAdapter.Listener() {

        @Override
        public void onRequestKanjiListForWord(int position, Word word) {
            mPresenter.onRequestKanjiListForWord(position, word);
        }

        @Override
        public void onRequestDetailForKanji(Kanji kanji) {
            mPresenter.onRequestDetailForKanji(kanji);
        }
    };
}
