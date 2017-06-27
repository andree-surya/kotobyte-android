package com.kotobyte.main;

interface MainPageContracts {

    interface View {

        void enableSearchButton(boolean enable);
        void showClearButton(boolean show);

        void showMigrationErrorDialog();
        void showMigrationProgressDialog(boolean show);
        boolean isMigrationProgressDialogShowing();

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
        void onClickRetryMigrationButton();

        void onChangeTextOnQueryEditor(CharSequence text);
        void onReceiveSearchRequest(CharSequence query);
    }
}
