package com.kotobyte.word

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.kotobyte.R
import com.kotobyte.base.ServiceLocator
import com.kotobyte.models.Word
import com.kotobyte.search.EntrySearchContracts
import com.kotobyte.search.EntrySearchFragment

class WordSearchFragment : EntrySearchFragment<Word>() {

    val query: String
        get() = arguments?.getString(ARG_QUERY) ?: ""

    override lateinit var emptySearchResultsLabel: String
    override lateinit var dataSource: EntrySearchContracts.DataSource<Word>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        emptySearchResultsLabel = getString(R.string.word_empty)
        dataSource = WordSearchDataSource(ServiceLocator.databaseProvider, query)
    }

    override fun createSearchResultsAdapter(entries: List<Word>): RecyclerView.Adapter<*> =

            WordSearchResultsAdapter(context, entries) { clickedWord ->

                startActivity(WordDetailsActivity.createIntent(context, clickedWord))
            }

    companion object {
        val ARG_QUERY = "query"

        fun create(query: String) = WordSearchFragment().apply {
            arguments = Bundle().apply { putString(ARG_QUERY, query) }
        }
    }
}
