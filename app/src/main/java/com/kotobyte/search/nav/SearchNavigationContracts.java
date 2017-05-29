package com.kotobyte.search.nav;

interface SearchNavigationContracts {

    interface View {

        void showClearButton(boolean show);
        void showAboutApplicationScreen();
        void showSearchResultsScreen(CharSequence query);

        void setTextOnQueryEditor(CharSequence text);
        void assignFocusToQueryEditor();
    }

    interface Presenter {

        void onClickClearButton();
        void onClickPasteMenuItem(CharSequence text);
        void onClickAboutMenuItem();

        void onChangeTextOnQueryEditor(CharSequence text);
        void onReceiveSearchRequest(CharSequence query);
    }
}
