package com.kotobyte.search

import com.kotobyte.models.Word

object EntrySearchContracts {

    interface View<in T> {

        fun showProgressBar(show: Boolean)
        fun showSearchResultsView(show: Boolean)
        fun showSearchResults(entries: List<T>)
        fun showNoSearchResultsLabel(show: Boolean)
        fun showUnknownError(error: Throwable)
    }

    interface Presenter {

        fun onCreate()
        fun onDestroy()
    }

    interface DataSource<out T> {

        fun searchEntries(): List<T>
    }
}