package com.kotobyte.search

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kotobyte.R
import com.kotobyte.databinding.FragmentEntrySearchBinding
import com.kotobyte.models.Entry
import com.kotobyte.utils.ErrorDialogFragment

abstract class EntrySearchFragment<T : Entry>: Fragment(), EntrySearchContracts.View<T> {

    abstract protected var emptyMessage: String get
    abstract protected var dataSource: EntrySearchContracts.DataSource<T> get
    abstract protected var searchResultsAdapter: EntrySearchResultsAdapter<T, *> get

    private lateinit var binding: FragmentEntrySearchBinding
    private lateinit var presenter: EntrySearchPresenter<T>

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_entry_search, container, false)
        binding.searchResultsView.adapter = searchResultsAdapter
        binding.noSearchResultsLabel.text = emptyMessage

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        presenter = EntrySearchPresenter(this, dataSource)
        presenter.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.onDestroy()
    }

    override fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showSearchResults(entries: List<T>) {
        searchResultsAdapter.entries = entries
    }

    override fun showNoSearchResultsLabel(show: Boolean) {
        binding.noSearchResultsLabel.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showUnknownError(error: Throwable) {
        Log.e(this.javaClass.simpleName, error.localizedMessage, error)

        ErrorDialogFragment.create().show(fragmentManager, ErrorDialogFragment::class.java.simpleName)
    }
}