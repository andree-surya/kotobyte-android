package com.kotobyte.search

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kotobyte.R
import com.kotobyte.databinding.FragmentEntrySearchBinding
import com.kotobyte.utils.ErrorDialogFragment

abstract class EntrySearchFragment<T>: Fragment(), EntrySearchContracts.View<T> {

    abstract protected var emptySearchResultsLabel: String get
    abstract protected var dataSource: EntrySearchContracts.DataSource<T> get

    abstract protected fun createSearchResultsAdapter(entries: List<T>): RecyclerView.Adapter<*>

    private lateinit var binding: FragmentEntrySearchBinding
    private lateinit var presenter: EntrySearchPresenter<T>

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_entry_search, container, false)
        binding.noSearchResultsLabel.text = emptySearchResultsLabel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        presenter = EntrySearchPresenter<T>(this, dataSource)
        presenter.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter.onDestroy()
    }

    override fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showSearchResultsView(show: Boolean) {
        binding.searchResultsView.animate()?.alpha(if (show) 1f else 0f)?.start()
    }

    override fun showSearchResults(entries: List<T>) {
        binding.searchResultsView.adapter = createSearchResultsAdapter(entries)
    }

    override fun showNoSearchResultsLabel(show: Boolean) {
        binding.noSearchResultsLabel.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showUnknownError(error: Throwable) {
        Log.e(this.javaClass.simpleName, error.localizedMessage, error)

        ErrorDialogFragment.create().show(fragmentManager, ErrorDialogFragment::class.java.simpleName)
    }

    companion object {
        val ARG_QUERIES = "queries"
    }
}