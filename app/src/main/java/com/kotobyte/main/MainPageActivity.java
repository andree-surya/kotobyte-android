package com.kotobyte.main;

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
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toolbar;

import com.kotobyte.R;
import com.kotobyte.base.DatabaseProvider;
import com.kotobyte.base.ServiceLocator;
import com.kotobyte.databinding.ActivityMainPageBinding;
import com.kotobyte.search.SearchPageFragment;
import com.kotobyte.utils.ErrorDialogFragment;
import com.kotobyte.utils.ProgressDialogFragment;

public class MainPageActivity extends FragmentActivity implements MainPageContracts.View {

    private static final String TAG = MainPageActivity.class.getSimpleName();

    private ActivityMainPageBinding mBinding;
    private MainPageContracts.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_page);
        mBinding.toolbar.inflateMenu(R.menu.menu_search);
        mBinding.toolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);
        mBinding.queryEditor.setOnEditorActionListener(mOnEditorActionListener);
        mBinding.queryEditor.addTextChangedListener(mQueryTextWatcher);
        mBinding.clearButton.setOnClickListener(mOnButtonClickListener);
        mBinding.searchButton.setOnClickListener(mOnButtonClickListener);

        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);

        prepareAndStartPresenter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPresenter.onDestroy();

        getSupportFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof ErrorDialogFragment) {
            ErrorDialogFragment errorDialogFragment = (ErrorDialogFragment) fragment;

            errorDialogFragment.setCancelable(false);
            errorDialogFragment.setCallback(mErrorDialogFragmentCallback);
        }
    }

    @Override
    public void enableSearchButton(boolean enable) {
        mBinding.searchButton.setEnabled(enable);
    }

    @Override
    public void showClearButton(boolean show) {
        mBinding.clearButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setTextOnQueryEditor(CharSequence text) {
        mBinding.queryEditor.setText(text);
        mBinding.queryEditor.selectAll();
    }

    @Override
    public void assignFocusToQueryEditor(boolean focus) {

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (focus) {
            mBinding.queryEditor.requestFocus();

            inputMethodManager.showSoftInput(mBinding.queryEditor, InputMethodManager.SHOW_IMPLICIT);

        } else {
            mBinding.queryEditor.clearFocus();

            inputMethodManager.hideSoftInputFromWindow(mBinding.queryEditor.getWindowToken(), 0);
        }
    }

    @Override
    public void showError(Throwable error) {
        Log.e(TAG, error.getLocalizedMessage(), error);

        ErrorDialogFragment.newInstance().show(getSupportFragmentManager(), ErrorDialogFragment.TAG);
    }

    @Override
    public void showMigrationErrorDialog() {

        ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(
                getString(R.string.main_migration_error_title),
                getString(R.string.main_migration_error_message));

        errorDialogFragment.show(getSupportFragmentManager(), ErrorDialogFragment.TAG);
    }

    @Override
    public void showMigrationProgressDialog(boolean show) {

        ProgressDialogFragment dialogFragment = (ProgressDialogFragment)
                getSupportFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);

        if (show) {

            if (dialogFragment == null) {

                dialogFragment = ProgressDialogFragment.newInstance(
                        getString(R.string.main_migration_progress_title),
                        getString(R.string.main_migration_progress_message));
            }

            if (! dialogFragment.isAdded()) {
                dialogFragment.show(getSupportFragmentManager(), ProgressDialogFragment.TAG);
            }

        } else if (dialogFragment != null) {

            dialogFragment.dismiss();
        }
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

    @Override
    public void showAboutApplicationScreen() {
        new AboutPageFragment().show(getSupportFragmentManager(), AboutPageFragment.TAG);
    }

    private void prepareAndStartPresenter() {

        DatabaseProvider databaseProvider = ServiceLocator.getInstance().getDatabaseProvider();

        mPresenter = new MainPagePresenter(this, databaseProvider);
        mPresenter.onCreate();

        if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
            mPresenter.onReceiveSearchRequest(getIntent().getStringExtra(Intent.EXTRA_TEXT));
        }
    }

    private CharSequence getPlainTextFromClipboard() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboardManager.hasPrimaryClip()) {
            return clipboardManager.getPrimaryClip().getItemAt(0).getText();
        }

        return null;
    }

    private ErrorDialogFragment.Callback mErrorDialogFragmentCallback = new ErrorDialogFragment.Callback() {

        @Override
        public void onClickPositiveButton() {
            mPresenter.onClickRetryButton();
        }
    };

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

    private View.OnClickListener mOnButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.search_button:
                    mPresenter.onReceiveSearchRequest(mBinding.queryEditor.getText());
                    break;

                case R.id.clear_button:
                    mPresenter.onClickClearButton();
                    break;

                default:
                    break;
            }
        }
    };

    private Toolbar.OnMenuItemClickListener mOnMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {

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
