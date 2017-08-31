package com.kotobyte.main

internal object MainSearchContracts {

    interface View {

        fun enableSearchButton(enable: Boolean)
        fun showClearButton(show: Boolean)

        fun showMigrationErrorDialog()
        fun showMigrationProgressDialog(show: Boolean)

        fun showSearchResultsScreen(query: CharSequence)
        fun showAboutApplicationScreen()

        fun setTextOnQueryEditor(text: CharSequence?)
        fun assignFocusToQueryEditor(focus: Boolean)

        fun showError(error: Throwable)
    }

    interface Presenter {

        fun onCreate()
        fun onDestroy()

        fun onClickClearButton()
        fun onClickPasteMenuItem(text: CharSequence)
        fun onClickAboutMenuItem()
        fun onClickRetryButton()

        fun onChangeTextOnQueryEditor(text: CharSequence)
        fun onReceiveSearchRequest(query: CharSequence)
    }
}
