package com.kotobyte;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kotobyte.databinding.FragmentSearchBinding;

/**
 * Created by andree.surya on 2016/12/29.
 */
public class SearchFragment extends Fragment {
    private static final String QUERY_KEY = "query";

    private String mQuery;
    private FragmentSearchBinding mBinding;

    public static SearchFragment newInstance(String query) {
        SearchFragment searchFragment = new SearchFragment();

        Bundle arguments = new Bundle();
        arguments.putString(QUERY_KEY, query);
        searchFragment.setArguments(arguments);

        return searchFragment;
    }

    public String getQuery() {
        return mQuery;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mQuery = getArguments().getString(QUERY_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);


        return mBinding.getRoot();
    }
}
