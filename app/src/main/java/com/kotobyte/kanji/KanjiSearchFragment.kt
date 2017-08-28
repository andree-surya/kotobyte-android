package com.kotobyte.kanji

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.kotobyte.R
import com.kotobyte.base.ServiceLocator
import com.kotobyte.models.Kanji
import com.kotobyte.search.EntrySearchContracts
import com.kotobyte.search.EntrySearchFragment

class KanjiSearchFragment : EntrySearchFragment<Kanji>() {

    override lateinit var emptySearchResultsLabel: String
    override lateinit var dataSource: EntrySearchContracts.DataSource<Kanji>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        emptySearchResultsLabel = getString(R.string.kanji_empty)

        dataSource = KanjiSearchDataSource(ServiceLocator.databaseProvider,
                arguments?.getStringArrayList(ARG_QUERIES) ?: arrayListOf())
    }

    override fun createSearchResultsAdapter(entries: List<Kanji>): RecyclerView.Adapter<*> =

            KanjiSearchResultsAdapter(context, entries, object : KanjiSearchResultsAdapter.Listener {

                override fun onClickKanji(kanji: Kanji) {

                    KanjiDetailsDialogFragment.create(kanji).show(fragmentManager,
                            KanjiDetailsDialogFragment::class.java.simpleName)
                }
            })

    companion object {
        private val ARG_QUERIES = "queries"

        fun create(queries: List<String>) = KanjiSearchFragment().apply {

            arguments = Bundle().apply {
                putStringArrayList(ARG_QUERIES, ArrayList(queries))
            }
        }
    }
}