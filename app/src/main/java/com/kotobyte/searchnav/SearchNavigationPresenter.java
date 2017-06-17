package com.kotobyte.searchnav;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class SearchNavigationPresenter implements SearchNavigationContracts.Presenter {

    private SearchNavigationContracts.View mView;
    private SearchNavigationContracts.DatabaseMigrationManager mDatabaseMigrationManager;

    private String mMostRecentSearchRequestQuery;
    private Disposable mDatabaseMigrationSubscription;

    private Scheduler mBackgroundScheduler = Schedulers.io();
    private Scheduler mMainThreadScheduler = AndroidSchedulers.mainThread();

    SearchNavigationPresenter(
            SearchNavigationContracts.View view,
            SearchNavigationContracts.DatabaseMigrationManager databaseMigrationManager) {

        mView = view;
        mDatabaseMigrationManager = databaseMigrationManager;
    }

    @Override
    public void onCreate() {

        mView.showClearButton(false);
        mView.enableSearchButton(false);

        executeDatabaseMigrationIfNeeded();
    }

    @Override
    public void onDestroy() {

        if (mDatabaseMigrationSubscription != null) {
            mDatabaseMigrationSubscription.dispose();
        }
    }

    @Override
    public void onClickClearButton() {

        mView.setTextOnQueryEditor(null);
        mView.assignFocusToQueryEditor(true);
    }

    @Override
    public void onClickPasteMenuItem(CharSequence text) {

        mView.setTextOnQueryEditor(text);
        mView.assignFocusToQueryEditor(true);
    }

    @Override
    public void onClickAboutMenuItem() {
        mView.showAboutApplicationScreen();
    }

    @Override
    public void onChangeTextOnQueryEditor(CharSequence text) {

        mView.showClearButton(text.length() > 0);
        mView.enableSearchButton(text.length() > 0);
    }

    @Override
    public void onReceiveSearchRequest(CharSequence query) {
        mMostRecentSearchRequestQuery = query.toString().trim();

        if (! mDatabaseMigrationManager.isMigrationInProgress()) {
            executeSearchForMostRecentQuery();
        }
    }

    private void executeSearchForMostRecentQuery() {

        if (mMostRecentSearchRequestQuery != null) {

            mView.setTextOnQueryEditor(mMostRecentSearchRequestQuery);
            mView.showSearchResultsScreen(mMostRecentSearchRequestQuery);

            mView.assignFocusToQueryEditor(false);
        }
    }

    private void executeDatabaseMigrationIfNeeded() {

        if (! mDatabaseMigrationManager.isMigrationNeeded()) {
            return;
        }

        if (mDatabaseMigrationManager.isMigrationInProgress()) {
            return;
        }

        mView.showMigrationProgressDialog();

        mDatabaseMigrationSubscription = mDatabaseMigrationManager.executeMigration()
                .subscribeOn(mBackgroundScheduler)
                .observeOn(mMainThreadScheduler)
                .subscribe(new Consumer<Boolean>() {

                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {

                        mView.closeMigrationProgressDialog();
                        executeSearchForMostRecentQuery();
                    }

                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {

                        mView.closeMigrationProgressDialog();
                        mView.showError(throwable);
                    }
                });
    }
}
