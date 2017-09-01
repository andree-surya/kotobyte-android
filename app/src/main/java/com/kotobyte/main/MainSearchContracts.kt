package com.kotobyte.main

internal object MainSearchContracts {

    interface View {

        fun showMigrationProgressDialog(show: Boolean)
        fun showSearchResultsScreen(query: String)
        fun expandSearchViewWithText(text: String?)
        fun showAboutApplicationScreen()

        fun showMigrationError(error: Throwable)
        fun showUnknownError(error: Throwable)
    }

    interface Presenter {

        fun onCreate()
        fun onDestroy()

        fun onClickAboutMenuItem()
        fun onClickSearchMenuItem()
    }
}
