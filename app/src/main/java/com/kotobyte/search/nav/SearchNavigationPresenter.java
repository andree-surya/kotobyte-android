package com.kotobyte.search.nav;

class SearchNavigationPresenter implements SearchNavigationContracts.Presenter {

    private SearchNavigationContracts.View mView;

    SearchNavigationPresenter(SearchNavigationContracts.View view) {
        mView = view;
    }

    @Override
    public void onCreate() {
        mView.assignFocusToQueryEditor(true);
    }

    @Override
    public void onDestroy() {

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
    }

    @Override
    public void onReceiveSearchRequest(CharSequence query) {

        mView.setTextOnQueryEditor(query);
        mView.assignFocusToQueryEditor(false);

        mView.showSearchResultsScreen(query);
    }
}
