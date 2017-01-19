package com.kotobyte;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kotobyte.databinding.FragmentSearchResultsBinding;
import com.kotobyte.model.Kanji;
import com.kotobyte.model.KanjiSearchResults;
import com.kotobyte.model.WordSearchResults;
import com.kotobyte.util.ProcessUtil;
import com.kotobyte.util.WebService;
import com.kotobyte.adapter.WordSearchResultsAdapter;

/**
 * Created by andree.surya on 2016/12/29.
 */
public class SearchResultsFragment extends Fragment implements
        SearchWorkerFragment.Listener, WordSearchResultsAdapter.Listener, View.OnClickListener {

    public static final String TAG = SearchResultsFragment.class.getSimpleName();

    private static final String QUERY_KEY = "query";

    // Minimum time (millis) between cell expansion and cell reload to prevent animation stutter.
    private static final long MIN_DELAY_BETWEEN_ANIMATION = 1000;

    private String mQuery;
    private long mLastExpansionTimestamp;

    private FragmentSearchResultsBinding mBinding;
    private SearchWorkerFragment mSearchWorkerFragment;
    private WordSearchResultsAdapter mWordSearchResultsAdapter;

    public static SearchResultsFragment newInstance(String query) {
        SearchResultsFragment searchResultsFragment = new SearchResultsFragment();

        Bundle arguments = new Bundle();
        arguments.putString(QUERY_KEY, query);
        searchResultsFragment.setArguments(arguments);

        return searchResultsFragment;
    }

    public String getQuery() {
        return mQuery;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mQuery = getArguments().getString(QUERY_KEY);

        mWordSearchResultsAdapter = new WordSearchResultsAdapter(getContext(), this);
        mSearchWorkerFragment = createSearchWorkerFragment();

        executeSearchSoon();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_results, container, false);

        mBinding.retryButton.setOnClickListener(this);
        mBinding.searchResultsView.setAdapter(mWordSearchResultsAdapter);

        return mBinding.getRoot();
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);

        if (childFragment instanceof SearchWorkerFragment) {
            ((SearchWorkerFragment) childFragment).setListener(this);
        }
    }

    private SearchWorkerFragment createSearchWorkerFragment() {

        SearchWorkerFragment fragment = (SearchWorkerFragment)
                getFragmentManager().findFragmentByTag(SearchWorkerFragment.TAG);

        if (fragment == null) {
            fragment = new SearchWorkerFragment();

            getChildFragmentManager()
                    .beginTransaction()
                    .add(fragment, SearchWorkerFragment.TAG)
                    .commit();
        }

        return fragment;
    }

    private void executeSearchSoon() {

        ProcessUtil.executeSoon(new Runnable() {

            @Override
            public void run() {
                mBinding.progressBar.setVisibility(View.VISIBLE);
                mBinding.retryButton.setVisibility(View.GONE);
                mBinding.problemTextView.setVisibility(View.GONE);

                mSearchWorkerFragment.searchWords(mQuery);
            }
        });
    }

    private void showSnackbarForError(WebService.Error error) {

        int messageResource = error == WebService.Error.NETWORK ?
                R.string.error_network : R.string.error_unknown;

        Snackbar.make(mBinding.searchResultsView, messageResource, Snackbar.LENGTH_LONG).show();
    }

    private void executeRunnableAfterRequiredAnimationDelay(Runnable runnable) {

        long timeEllapsedAfterLastExpansion = System.currentTimeMillis() - mLastExpansionTimestamp;

        if (timeEllapsedAfterLastExpansion >= MIN_DELAY_BETWEEN_ANIMATION) {
            runnable.run();

        } else {
            long remainingTime = MIN_DELAY_BETWEEN_ANIMATION - timeEllapsedAfterLastExpansion;

            ProcessUtil.executeAfterDelay(remainingTime, runnable);
        }

        mLastExpansionTimestamp = 0;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.retry_button) {
            executeSearchSoon();
        }
    }

    @Override
    public void onClickWordAtPosition(int position) {

        if (mWordSearchResultsAdapter.getExpandedCellPosition() == position) {

            mWordSearchResultsAdapter.collapseCellAtPosition(position);
            mLastExpansionTimestamp = 0;

        } else {

            if (! mWordSearchResultsAdapter.hasKanjiSearchResultsAtPosition(position)) {
                mSearchWorkerFragment.searchKanji(position);
            }

            mWordSearchResultsAdapter.expandCellAtPosition(position);
            mLastExpansionTimestamp = System.currentTimeMillis();
        }
    }

    @Override
    public void onClickKanji(Kanji kanji) {

    }

    @Override
    public void onReceiveWordSearchResults(WordSearchResults wordSearchResults) {
        mBinding.progressBar.setVisibility(View.GONE);

        if (wordSearchResults.getSize() > 0) {
            mWordSearchResultsAdapter.setWordsSearchResults(wordSearchResults);

        } else {
            mBinding.problemTextView.setVisibility(View.VISIBLE);
            mBinding.problemTextView.setText(getString(R.string.error_no_results, mQuery));
        }
    }

    @Override
    public void onReceiveErrorWhileSearchingForWords(WebService.Error error) {
        mBinding.progressBar.setVisibility(View.GONE);
        mBinding.retryButton.setVisibility(View.VISIBLE);
        mBinding.problemTextView.setVisibility(View.VISIBLE);
        mBinding.problemTextView.setText(getString(R.string.error_search, mQuery));

        showSnackbarForError(error);
    }

    @Override
    public void onReceiveKanjiSearchResults(final KanjiSearchResults kanjiSearchResults, final int position) {

        executeRunnableAfterRequiredAnimationDelay(new Runnable() {

            @Override
            public void run() {
                mWordSearchResultsAdapter.setKanjiSearchResults(position, kanjiSearchResults);
            }
        });
    }

    @Override
    public void onReceiveErrorWhileSearchingForKanji(final WebService.Error error, final int position) {

        executeRunnableAfterRequiredAnimationDelay(new Runnable() {

            @Override
            public void run() {
                mWordSearchResultsAdapter.collapseCellAtPosition(position);

                showSnackbarForError(error);
            }
        });
    }
}
