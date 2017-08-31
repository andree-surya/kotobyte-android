package com.kotobyte.search

import com.kotobyte.models.Entry
import com.kotobyte.utils.AsynchronousTask

class EntrySearchPresenter<T : Entry>(
        val view: EntrySearchContracts.View<T>,
        val dataSource: EntrySearchContracts.DataSource<T>

) : EntrySearchContracts.Presenter {

    private var searchTask: AsynchronousTask<*>? = null

    override fun onCreate() {
        searchEntries()
    }

    override fun onDestroy() {
        searchTask?.cancel(true)
    }

    private fun searchEntries() {
        searchTask = SearchEntriesTask().apply { execute() }
    }

    private inner class SearchEntriesTask : AsynchronousTask<List<T>>() {

        override fun doInBackground(): List<T> = dataSource.searchEntries()

        override fun onPreExecute() {

            view.showProgressBar(true)
            view.showNoSearchResultsLabel(false)
        }

        override fun onPostExecute(data: List<T>?, error: Throwable?) {
            view.showProgressBar(false)

            if (error == null) {

                if (data != null && data.isNotEmpty()) {
                    view.showSearchResults(data)

                } else {
                    view.showNoSearchResultsLabel(true)
                }

            } else {
                view.showUnknownError(error)
            }
        }
    }
}