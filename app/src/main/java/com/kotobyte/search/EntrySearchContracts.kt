package com.kotobyte.search

import com.kotobyte.models.Entry

object EntrySearchContracts {

    interface View<in T : Entry> {

        fun showProgressBar(show: Boolean)
        fun showSearchResults(entries: List<T>)
        fun showNoSearchResultsLabel(show: Boolean)
        fun showUnknownError(error: Throwable)
    }

    interface Presenter {

        fun onCreate()
        fun onDestroy()
    }

    interface DataSource<out T : Entry> {

        fun searchEntries(): List<T>
    }
}