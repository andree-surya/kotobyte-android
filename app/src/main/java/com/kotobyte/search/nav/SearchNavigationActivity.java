package com.kotobyte.search.nav;

import android.app.SearchManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toolbar;

import com.kotobyte.R;
import com.kotobyte.databinding.ActivitySearchNavigationBinding;
import com.kotobyte.search.page.SearchPageFragment;

public class SearchNavigationActivity extends FragmentActivity implements SearchNavigationContracts.View {

    private ActivitySearchNavigationBinding mBinding;
    private SearchNavigationContracts.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_search_navigation);
        mBinding.toolbar.inflateMenu(R.menu.menu_search);
        mBinding.toolbar.setOnMenuItemClickListener(mOnMenuitemClickListener);
        mBinding.queryEditor.setOnEditorActionListener(mOnEditorActionListener);
        mBinding.queryEditor.addTextChangedListener(mQueryTextWatcher);
        mBinding.clearButton.setOnClickListener(mOnClearButtonClickListener);

        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);

        mPresenter = new SearchNavigationPresenter(this);

        handleIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getSupportFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    @Override
    public void showClearButton(boolean show) {
        mBinding.clearButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setTextOnQueryEditor(CharSequence text) {
        mBinding.queryEditor.setText(text);
    }

    @Override
    public void assignFocusToQueryEditor() {

        mBinding.queryEditor.requestFocus();
        mBinding.queryEditor.selectAll();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(mBinding.queryEditor, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void showAboutApplicationScreen() {
        new AboutPageDialogFragment().show(getSupportFragmentManager(), AboutPageDialogFragment.TAG);
    }

    @Override
    public void showSearchResultsScreen(CharSequence query) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, SearchPageFragment.newInstance(query), SearchPageFragment.TAG);

        if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            mPresenter.onReceiveSearchRequest(query);
        }

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            String query = intent.getStringExtra(Intent.EXTRA_TEXT);

            mPresenter.onReceiveSearchRequest(query);
        }
    }

    private CharSequence getPlainTextFromClipboard() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboardManager.hasPrimaryClip()) {
            return clipboardManager.getPrimaryClip().getItemAt(0).getText();
        }

        return null;
    }

    private TextWatcher mQueryTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            mPresenter.onChangeTextOnQueryEditor(s);
        }
    };

    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {

            if (textView.length() > 0) {

                boolean actionFromSoftKeyboard = actionId == EditorInfo.IME_ACTION_SEARCH;
                boolean actionFromHardKeyboard = actionId == EditorInfo.IME_ACTION_UNSPECIFIED;
                boolean isPressedDownEvent = event != null && event.getAction() == KeyEvent.ACTION_DOWN;

                if (actionFromSoftKeyboard || (actionFromHardKeyboard && isPressedDownEvent)) {
                    mPresenter.onReceiveSearchRequest(textView.getText());

                    return true;
                }
            }

            return false;
        }
    };

    private View.OnClickListener mOnClearButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mPresenter.onClickClearButton();
        }
    };

    private Toolbar.OnMenuItemClickListener mOnMenuitemClickListener = new Toolbar.OnMenuItemClickListener() {

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.action_about:
                    mPresenter.onClickAboutMenuItem();

                    return true;

                case R.id.action_paste:
                    mPresenter.onClickPasteMenuItem(getPlainTextFromClipboard());
                    return true;

                default:
                    return false;
            }
        }
    };

    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {

        @Override
        public void onBackStackChanged() {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (fragment instanceof SearchPageFragment) {
                setTextOnQueryEditor(((SearchPageFragment) fragment).getQuery());

            } else {
                setTextOnQueryEditor(null);
            }
        }
    };
}
