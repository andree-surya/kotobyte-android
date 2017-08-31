package com.kotobyte.sentence

import android.os.Bundle
import com.kotobyte.R
import com.kotobyte.base.ServiceLocator
import com.kotobyte.models.Sentence
import com.kotobyte.search.EntrySearchContracts
import com.kotobyte.search.EntrySearchFragment

class SentenceSearchFragment : EntrySearchFragment<Sentence>() {

    override lateinit var emptySearchResultsLabel: String
    override lateinit var dataSource: EntrySearchContracts.DataSource<Sentence>

    private val sentenceSearchResultsAdapterListener = object : SentenceSearchResultsAdapter.Listener {

        override fun onClickText(text: String) {
            TODO("not implemented")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        emptySearchResultsLabel = getString(R.string.sentence_empty)

        dataSource = SentenceSearchDataSource(ServiceLocator.databaseProvider,
                arguments?.getStringArrayList(ARG_QUERIES) ?: arrayListOf())
    }

    override fun createSearchResultsAdapter(entries: List<Sentence>) =
        SentenceSearchResultsAdapter(context, entries, sentenceSearchResultsAdapterListener)

    companion object {
        private val ARG_QUERIES = "queries"

        fun create(queries: List<String>) = SentenceSearchFragment().apply {

            arguments = Bundle().apply {
                putStringArrayList(ARG_QUERIES, ArrayList(queries))
            }
        }
    }
}