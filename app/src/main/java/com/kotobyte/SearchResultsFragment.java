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
import com.kotobyte.model.SearchResults;
import com.kotobyte.util.ProcessUtil;
import com.kotobyte.util.WebService;

/**
 * Created by andree.surya on 2016/12/29.
 */
public class SearchResultsFragment extends Fragment {

    private static final String QUERY_KEY = "query";
    private static final String SEARCH_WORKER_FRAGMENT_TAG = "search_worker_fragment";

    private String mQuery;
    private FragmentSearchResultsBinding mBinding;
    private SearchWorkerFragment mSearchWorkerFragment;
    private SearchResultsAdapter mSearchResultsAdapter;

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
        mSearchResultsAdapter = new SearchResultsAdapter(getContext());

        prepareSearchWorkerFragment();
        executeSearchLater();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_results, container, false);

        mBinding.retryButton.setOnClickListener(mOnRetryButtonClickListener);
        mBinding.searchResultsView.setAdapter(mSearchResultsAdapter);

        return mBinding.getRoot();
    }

    private void prepareSearchWorkerFragment() {

        mSearchWorkerFragment = (SearchWorkerFragment)
                getFragmentManager().findFragmentByTag(SEARCH_WORKER_FRAGMENT_TAG);

        if (mSearchWorkerFragment == null) {

            mSearchWorkerFragment = new SearchWorkerFragment();
            mSearchWorkerFragment.setQuery(mQuery);

            getFragmentManager()
                    .beginTransaction()
                    .add(mSearchWorkerFragment, SEARCH_WORKER_FRAGMENT_TAG)
                    .commit();
        }

        mSearchWorkerFragment.setListener(mSearchWorkerFragmentListener);
    }

    private void executeSearchLater() {

        ProcessUtil.executeLater(new Runnable() {

            @Override
            public void run() {
                mSearchWorkerFragment.execute();
            }
        });
    }

    private SearchWorkerFragment.Listener mSearchWorkerFragmentListener = new SearchWorkerFragment.Listener() {

        @Override
        public void onStartSearching() {
            mBinding.progressBar.setVisibility(View.VISIBLE);
            mBinding.retryButton.setVisibility(View.GONE);
            mBinding.problemTextView.setVisibility(View.GONE);
        }

        @Override
        public void onFinish(SearchResults searchResults) {
            mBinding.progressBar.setVisibility(View.GONE);

            if (searchResults.getSize() > 0) {
                mSearchResultsAdapter.setSearchResults(searchResults);

            } else {
                mBinding.problemTextView.setVisibility(View.VISIBLE);
                mBinding.problemTextView.setText(getString(R.string.error_no_results, mQuery));
            }
        }

        @Override
        public void onError(WebService.Error error) {
            mBinding.progressBar.setVisibility(View.GONE);
            mBinding.retryButton.setVisibility(View.VISIBLE);
            mBinding.problemTextView.setVisibility(View.VISIBLE);
            mBinding.problemTextView.setText(getString(R.string.error_search, mQuery));

            int messageResource = error == WebService.Error.NETWORK ?
                    R.string.error_network : R.string.error_unknown;

            Snackbar.make(mBinding.searchResultsView, messageResource, Snackbar.LENGTH_LONG).show();
        }
    };

    private View.OnClickListener mOnRetryButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mSearchWorkerFragment.execute();
        }
    };
}
