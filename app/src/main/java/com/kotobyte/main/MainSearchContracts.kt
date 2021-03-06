package com.kotobyte.main

internal object MainSearchContracts {

    interface View {

        fun showMigrationProgressDialog(show: Boolean)
        fun showSearchResultsScreen(query: String)
        fun showSearchViewWithText(text: String?)
        fun showEmptyPlaceholderLogo()
        fun showAboutApplicationScreen()

        fun showMigrationError(error: Throwable)
        fun showUnknownError(error: Throwable)

        fun readTextFromClipboard(): String
    }

    interface Presenter {

        fun onCreate()
        fun onDestroy()

        fun onClickAboutMenuItem()
        fun onClickPasteMenuItem()
        fun onClickSearchMenuItem()
    }
}
