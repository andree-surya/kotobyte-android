package com.kotobyte.word

import android.os.Bundle
import com.kotobyte.R
import com.kotobyte.base.ServiceLocator
import com.kotobyte.models.Word
import com.kotobyte.search.EntrySearchContracts
import com.kotobyte.search.EntrySearchFragment
import com.kotobyte.search.EntrySearchResultsAdapter

class WordSearchFragment : EntrySearchFragment<Word>() {

    private val query: String
        get() = arguments?.getString(ARG_QUERY) ?: ""

    override lateinit var emptyMessage: String
    override lateinit var dataSource: EntrySearchContracts.DataSource<Word>
    override lateinit var searchResultsAdapter: EntrySearchResultsAdapter<Word, *>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        emptyMessage = getString(R.string.word_empty)
        dataSource = WordSearchDataSource(ServiceLocator.databaseProvider, query)

        searchResultsAdapter = WordSearchResultsAdapter(context) { clickedWord ->
            startActivity(WordDetailsActivity.createIntent(context, clickedWord))
        }
    }

    companion object {
        val ARG_QUERY = "query"

        fun create(query: String) = WordSearchFragment().apply {
            arguments = Bundle().apply { putString(ARG_QUERY, query) }
        }
    }
}
