package com.kotobyte.main;

import com.kotobyte.base.DatabaseConnection;
import com.kotobyte.base.DatabaseProvider;

import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class MainPagePresenter implements MainPageContracts.Presenter {

    private MainPageContracts.View mView;
    private DatabaseProvider mDatabaseProvider;

    private String mMostRecentSearchRequestQuery;
    private Disposable mDatabaseMigrationSubscription;

    private Scheduler mBackgroundScheduler = Schedulers.io();
    private Scheduler mMainThreadScheduler = AndroidSchedulers.mainThread();

    MainPagePresenter(MainPageContracts.View view, DatabaseProvider databaseProvider) {

        mView = view;
        mDatabaseProvider = databaseProvider;
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
    public void onClickRetryMigrationButton() {
        executeDatabaseMigrationIfNeeded();
    }

    @Override
    public void onChangeTextOnQueryEditor(CharSequence text) {

        mView.showClearButton(text.length() > 0);
        mView.enableSearchButton(text.length() > 0);
    }

    @Override
    public void onReceiveSearchRequest(CharSequence query) {
        String searchRequestQuery = query.toString().trim();

        if (! searchRequestQuery.isEmpty()) {
            mMostRecentSearchRequestQuery = searchRequestQuery;

            if (! mView.isMigrationProgressDialogShowing()) {
                executeSearchForMostRecentQuery();
            }
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

        if (! mDatabaseProvider.isMigrationNeeded()) {
            return;
        }

        if (mDatabaseMigrationSubscription != null) {
            return;
        }

        if (mView.isMigrationProgressDialogShowing()) {
            return;
        }

        if (! mDatabaseProvider.isMigrationPossible()) {
            mView.showMigrationErrorDialog();

            return;
        }

        mView.showMigrationProgressDialog(true);

        mDatabaseMigrationSubscription = Single.fromCallable(new MigrateDatabase(mDatabaseProvider))
                .subscribeOn(mBackgroundScheduler)
                .observeOn(mMainThreadScheduler)
                .subscribe(new Consumer<DatabaseConnection>() {

                    @Override
                    public void accept(@NonNull DatabaseConnection connection) throws Exception {

                        mDatabaseMigrationSubscription = null;

                        mView.showMigrationProgressDialog(false);

                        executeSearchForMostRecentQuery();
                    }

                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {

                        mDatabaseMigrationSubscription = null;

                        mView.showMigrationProgressDialog(false);
                        mView.showError(throwable);
                    }
                });
    }

    private static class MigrateDatabase implements Callable<DatabaseConnection> {

        private DatabaseProvider mDatabaseProvider;

        private MigrateDatabase(DatabaseProvider databaseProvider) {
            mDatabaseProvider = databaseProvider;
        }

        @Override
        public DatabaseConnection call() throws Exception {
            return mDatabaseProvider.getConnection();
        }
    }
}
