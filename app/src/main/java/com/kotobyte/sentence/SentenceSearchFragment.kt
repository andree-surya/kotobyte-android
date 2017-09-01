package com.kotobyte.sentence

import android.os.Bundle
import com.kotobyte.R
import com.kotobyte.base.ServiceLocator
import com.kotobyte.main.MainSearchActivity
import com.kotobyte.models.Sentence
import com.kotobyte.search.EntrySearchContracts
import com.kotobyte.search.EntrySearchFragment
import com.kotobyte.search.EntrySearchResultsAdapter

class SentenceSearchFragment : EntrySearchFragment<Sentence>() {

    override lateinit var emptyMessage: String
    override lateinit var dataSource: EntrySearchContracts.DataSource<Sentence>
    override lateinit var searchResultsAdapter: EntrySearchResultsAdapter<Sentence, *>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val queries = arguments?.getStringArrayList(ARG_QUERIES) ?: arrayListOf()

        emptyMessage = getString(R.string.word_empty_sentence)
        dataSource = SentenceSearchDataSource(ServiceLocator.databaseProvider, queries)

        searchResultsAdapter = SentenceSearchResultsAdapter(context) { clickedToken ->
            startActivity(MainSearchActivity.createIntent(context, clickedToken.lemma))
        }
    }

    companion object {
        private val ARG_QUERIES = "queries"

        fun create(queries: List<String>) = SentenceSearchFragment().apply {

            arguments = Bundle().apply {
                putStringArrayList(ARG_QUERIES, ArrayList(queries))
            }
        }
    }
}