package com.kotobyte.main

import com.kotobyte.base.DatabaseProvider

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

internal class MainPagePresenter(private val view: MainPageContracts.View, private val databaseProvider: DatabaseProvider) : MainPageContracts.Presenter {

    private var mostRecentSearchRequestQuery: String? = null
    private var databaseMigrationSubscription: Disposable? = null

    override fun onCreate() {

        view.showClearButton(false)
        view.enableSearchButton(false)

        executeDatabaseMigrationIfNeeded()
    }

    override fun onDestroy() {
        databaseMigrationSubscription?.dispose()
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

        if (databaseMigrationSubscription != null) {
            return
        }

        if (!databaseProvider.isMigrationPossible) {
            view.showMigrationErrorDialog()

            return
        }

        databaseMigrationSubscription = Single.fromCallable({ databaseProvider.obtainDatabaseConnection() })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    databaseMigrationSubscription = null

                    view.showMigrationProgressDialog(false)

                    executeSearchForMostRecentQuery()

                }) { throwable ->

                    databaseMigrationSubscription = null

                    view.showMigrationProgressDialog(false)
                    view.showError(throwable)
                }
    }
}
