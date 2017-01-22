package com.kotobyte;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.kotobyte.databinding.ActivitySearchBinding;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding mBinding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_search);

        mBinding.toolbar.inflateMenu(R.menu.menu_search);
        mBinding.toolbar.setOnMenuItemClickListener(mOnMenuitemClickListener);

        mBinding.queryEditText.setOnEditorActionListener(mOnEditorActionListener);
        mBinding.queryEditText.addTextChangedListener(mQueryTextWatcher);

        mBinding.clearImageButton.setOnClickListener(mOnClearButtonClickListener);

        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_SEARCH.equals(action)) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            executeSearch(query);

            mBinding.clearImageButton.setVisibility(View.VISIBLE);
            mBinding.queryEditText.setText(query);

            setFocusOnQueryEditText(false);
        }

        if (Intent.ACTION_MAIN.equals(action)) {
            setFocusOnQueryEditText(true);
        }
    }

    private void executeSearch(String query) {
        SearchResultsFragment searchResultsFragment = SearchResultsFragment.newInstance(query);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, searchResultsFragment, SearchResultsFragment.TAG)
                .addToBackStack(null)
                .commit();

        setFocusOnQueryEditText(false);
    }

    private void setFocusOnQueryEditText(boolean shouldFocus) {

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (shouldFocus) {
            mBinding.queryEditText.requestFocus();

            inputMethodManager.showSoftInput(
                    mBinding.queryEditText, InputMethodManager.SHOW_IMPLICIT);

        } else {
            mBinding.queryEditText.clearFocus();

            inputMethodManager.hideSoftInputFromWindow(
                    mBinding.queryEditText.getWindowToken(), 0);
        }
    }

    private TextWatcher mQueryTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            mBinding.clearImageButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
        }
    };

    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {

            if (textView.length() > 0) {

                boolean acceptableAction = actionId == EditorInfo.IME_ACTION_SEARCH ||
                        ((actionId == EditorInfo.IME_ACTION_UNSPECIFIED && event.getAction() == KeyEvent.ACTION_DOWN));

                if (acceptableAction) {
                    executeSearch(textView.getText().toString());

                    return true;
                }
            }

            return false;
        }
    };

    private View.OnClickListener mOnClearButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mBinding.queryEditText.setText(null);

            setFocusOnQueryEditText(true);
        }
    };

    private Toolbar.OnMenuItemClickListener mOnMenuitemClickListener = new Toolbar.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.action_clear:
                    return true;

                default:
                    return false;
            }
        }
    };

    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {

        @Override
        public void onBackStackChanged() {

            String query = null;
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (fragment instanceof SearchResultsFragment) {
                query = ((SearchResultsFragment) fragment).getQuery();
            }

            mBinding.queryEditText.setText(query);

            if (query != null) {
                mBinding.queryEditText.setSelection(query.length());
            }
        }
    };
}
