package com.kotobyte.searchnav;

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
import android.widget.Toast;
import android.widget.Toolbar;

import com.kotobyte.R;
import com.kotobyte.base.Configuration;
import com.kotobyte.base.ServiceLocator;
import com.kotobyte.databinding.ActivitySearchNavigationBinding;
import com.kotobyte.searchpage.SearchPageFragment;
import com.kotobyte.utils.ProgressDialogFragment;

public class SearchNavigationActivity extends FragmentActivity implements SearchNavigationContracts.View {

    private static final String TAG = SearchNavigationActivity.class.getSimpleName();
    private static final String MIGRATION_PROGRESS_DIALOG_TAG = "migration_progress_dialog";

    private ActivitySearchNavigationBinding mBinding;
    private SearchNavigationContracts.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_search_navigation);
        mBinding.toolbar.inflateMenu(R.menu.menu_search);
        mBinding.toolbar.setOnMenuItemClickListener(mOnMenuItemClickListener);
        mBinding.queryEditor.setOnEditorActionListener(mOnEditorActionListener);
        mBinding.queryEditor.addTextChangedListener(mQueryTextWatcher);
        mBinding.clearButton.setOnClickListener(mOnButtonClickListener);
        mBinding.searchButton.setOnClickListener(mOnButtonClickListener);

        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);

        prepareAndKickStartPresenter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPresenter.onDestroy();

        getSupportFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);
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

        Toast.makeText(this, R.string.common_unknown_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMigrationProgressDialog() {

        ProgressDialogFragment.newInstance(getString(R.string.search_nav_migration))
                .show(getSupportFragmentManager(), MIGRATION_PROGRESS_DIALOG_TAG);
    }

    @Override
    public void closeMigrationProgressDialog() {

        ProgressDialogFragment progressDialogFragment = (ProgressDialogFragment)
                getSupportFragmentManager().findFragmentByTag(MIGRATION_PROGRESS_DIALOG_TAG);

        if (progressDialogFragment != null) {
            progressDialogFragment.dismiss();
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
        new AboutPageDialogFragment().show(getSupportFragmentManager(), AboutPageDialogFragment.TAG);
    }

    private void prepareAndKickStartPresenter() {

        Configuration configuration = ServiceLocator.getInstance().getConfiguration();

        mPresenter = new SearchNavigationPresenter(this,
                new DatabaseMigrationManager(configuration, getAssets()));

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
