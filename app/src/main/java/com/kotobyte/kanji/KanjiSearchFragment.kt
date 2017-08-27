package com.kotobyte.kanji

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.kotobyte.R
import com.kotobyte.base.ServiceLocator
import com.kotobyte.models.Kanji
import com.kotobyte.search.EntrySearchContracts
import com.kotobyte.search.EntrySearchFragment
import com.kotobyte.search.EntrySearchPresenter

class KanjiSearchFragment : EntrySearchFragment<Kanji>() {

    override lateinit var emptySearchResultsLabel: String
    override lateinit var presenter: EntrySearchContracts.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val queries = arguments?.getStringArrayList(ARG_QUERIES) ?: arrayListOf()
        val dataSource = KanjiSearchDataSource(ServiceLocator.databaseProvider, queries)

        emptySearchResultsLabel = getString(R.string.kanji_empty)
        presenter = EntrySearchPresenter<Kanji>(this, dataSource)
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