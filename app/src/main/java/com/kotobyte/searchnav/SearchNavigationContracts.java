package com.kotobyte.searchnav;

import io.reactivex.Single;

interface SearchNavigationContracts {

    interface View {

        void enableSearchButton(boolean enable);
        void showClearButton(boolean show);
        void showMigrationProgressDialog();
        void closeMigrationProgressDialog();
        void showSearchResultsScreen(CharSequence query);
        void showAboutApplicationScreen();

        void setTextOnQueryEditor(CharSequence text);
        void assignFocusToQueryEditor(boolean focus);

        void showError(Throwable error);
    }

    interface Presenter {

        void onCreate();
        void onDestroy();

        void onClickClearButton();
        void onClickPasteMenuItem(CharSequence text);
        void onClickAboutMenuItem();

        void onChangeTextOnQueryEditor(CharSequence text);
        void onReceiveSearchRequest(CharSequence query);
    }

    interface DatabaseMigrationManager {

        boolean isMigrationNeeded();
        boolean isMigrationInProgress();

        /**
         * Execute database migration.
         * @return A single observable emitting boolean whose value is always true (to be ignored).
         */
        Single<Boolean> executeMigration();
    }
}
