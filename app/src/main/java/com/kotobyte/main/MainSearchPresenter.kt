package com.kotobyte.main

import com.kotobyte.base.DatabaseConnection
import com.kotobyte.base.DatabaseProvider
import com.kotobyte.utils.AsynchronousTask

internal class MainSearchPresenter(private val view: MainSearchContracts.View, private val databaseProvider: DatabaseProvider) : MainSearchContracts.Presenter {

    private var mostRecentSearchRequestQuery: String? = null
    private var databaseMigrationTask: AsynchronousTask<*>? = null

    override fun onCreate() {

        view.showClearButton(false)
        view.enableSearchButton(false)

        executeDatabaseMigrationIfNeeded()
    }

    override fun onDestroy() {
        databaseMigrationTask?.cancel(true)
    }

    override fun onClickClearButton() {

        view.setTextOnQueryEditor("")
        view.assignFocusToQueryEditor(true)
    }

    override fun onClickPasteMenuItem(text: CharSequence) {

        view.setTextOnQueryEditor(text)
        view.assignFocusToQueryEditor(true)
    }

    override fun onClickAboutMenuItem() = view.showAboutApplicationScreen()

    override fun onClickRetryButton() = executeDatabaseMigrationIfNeeded()

    override fun onChangeTextOnQueryEditor(text: CharSequence) {

        view.showClearButton(text.isNotEmpty())
        view.enableSearchButton(text.isNotEmpty())
    }

    override fun onReceiveSearchRequest(query: CharSequence) {
        val searchRequestQuery = query.toString().trim { it <= ' ' }

        if (searchRequestQuery.isNotEmpty()) {
            mostRecentSearchRequestQuery = searchRequestQuery

            if (!databaseProvider.isMigrationInProgress) {
                executeSearchForMostRecentQuery()
            }
        }
    }

    private fun executeSearchForMostRecentQuery() {

        val mostRecentSearchRequestQuery = mostRecentSearchRequestQuery

        if (mostRecentSearchRequestQuery != null) {

            view.setTextOnQueryEditor(mostRecentSearchRequestQuery)
            view.showSearchResultsScreen(mostRecentSearchRequestQuery)

            view.assignFocusToQueryEditor(false)
        }
    }

    private fun executeDatabaseMigrationIfNeeded() {

        if (!databaseProvider.isMigrationNeeded) {
            return
        }

        if (databaseMigrationTask != null) {
            return
        }

        if (!databaseProvider.isMigrationPossible) {
            view.showMigrationErrorDialog()

            return
        }

        databaseMigrationTask = MigrateDatabaseTask().apply { execute() }
    }

    private inner class MigrateDatabaseTask : AsynchronousTask<DatabaseConnection>() {

        override fun doInBackground() = databaseProvider.obtainDatabaseConnection()

        override fun onPreExecute() {
            view.showMigrationProgressDialog(true)
        }

        override fun onPostExecute(data: DatabaseConnection?, error: Throwable?) {
            view.showMigrationProgressDialog(false)

            if (error == null) {
                executeSearchForMostRecentQuery()

            } else {
                view.showError(error)
            }

            databaseMigrationTask = null
        }
    }
}
