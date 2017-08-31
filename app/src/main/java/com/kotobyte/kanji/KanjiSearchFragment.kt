package com.kotobyte.kanji

import android.os.Bundle
import com.kotobyte.R
import com.kotobyte.base.ServiceLocator
import com.kotobyte.models.Kanji
import com.kotobyte.search.EntrySearchContracts
import com.kotobyte.search.EntrySearchFragment
import com.kotobyte.search.EntrySearchResultsAdapter

class KanjiSearchFragment : EntrySearchFragment<Kanji>() {

    override lateinit var emptyMessage: String
    override lateinit var dataSource: EntrySearchContracts.DataSource<Kanji>
    override lateinit var searchResultsAdapter: EntrySearchResultsAdapter<Kanji, *>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val queries = arguments?.getStringArrayList(ARG_QUERIES) ?: arrayListOf()

        emptyMessage = getString(R.string.kanji_empty)
        dataSource = KanjiSearchDataSource(ServiceLocator.databaseProvider, queries)

        searchResultsAdapter = KanjiSearchResultsAdapter(context) { clickedKanji ->

            KanjiDetailsDialogFragment.create(clickedKanji)
                    .show(fragmentManager, KanjiDetailsDialogFragment::class.java.simpleName)
        }
    }

    companion object {
        private val ARG_QUERIES = "queries"

        fun create(queries: List<String>) = KanjiSearchFragment().apply {

            arguments = Bundle().apply {
                putStringArrayList(ARG_QUERIES, ArrayList(queries))
            }
        }
    }
}