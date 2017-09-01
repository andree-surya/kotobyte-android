package com.kotobyte.main

import com.kotobyte.base.DatabaseConnection
import com.kotobyte.base.DatabaseProvider
import com.kotobyte.utils.AsynchronousTask

internal class MainSearchPresenter(
        private val view: MainSearchContracts.View,
        private val databaseProvider: DatabaseProvider,
        private val searchQuery: String?

) : MainSearchContracts.Presenter {

    private var databaseMigrationTask: AsynchronousTask<*>? = null

    override fun onCreate() {

        if (databaseProvider.isMigrationNeeded) {
            initiateDatabaseMigration()

        } else {
            initiateSearchTask()
        }
    }

    override fun onDestroy() {
        databaseMigrationTask?.cancel(true)
    }

    override fun onClickAboutMenuItem() {
        view.showAboutApplicationScreen()
    }

    override fun onClickSearchMenuItem() {
        view.expandSearchViewWithText(searchQuery)
    }

    private fun initiateSearchTask() {

        if (searchQuery != null) {
            view.showSearchResultsScreen(searchQuery)

        } else {
            view.expandSearchViewWithText(searchQuery)
        }
    }

    private fun initiateDatabaseMigration() {

        when {
            databaseProvider.isMigrationInProgress ->
                view.showMigrationProgressDialog(true)

            databaseProvider.isMigrationPossible ->
                databaseMigrationTask = MigrateDatabaseTask().apply { execute() }

            else -> view.showMigrationError(RuntimeException("Not enough space for database."))
        }
    }

    private inner class MigrateDatabaseTask : AsynchronousTask<DatabaseConnection>() {

        override fun doInBackground(): DatabaseConnection =
                databaseProvider.obtainDatabaseConnection()

        override fun onPreExecute() {
            view.showMigrationProgressDialog(true)
        }

        override fun onPostExecute(data: DatabaseConnection?, error: Throwable?) {
            view.showMigrationProgressDialog(false)

            if (error == null) {
                initiateSearchTask()

            } else {
                view.showMigrationError(error)
            }

            databaseMigrationTask = null
        }
    }
}
