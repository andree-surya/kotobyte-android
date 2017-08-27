package com.kotobyte.word

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.kotobyte.R
import com.kotobyte.base.ServiceLocator
import com.kotobyte.models.Word
import com.kotobyte.search.EntrySearchContracts
import com.kotobyte.search.EntrySearchFragment
import com.kotobyte.search.EntrySearchPresenter

class WordSearchFragment : EntrySearchFragment<Word>() {

    val query: String
        get() = arguments?.getString(ARG_QUERY) ?: ""

    override lateinit var emptySearchResultsLabel: String
    override lateinit var presenter: EntrySearchContracts.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataSource = WordSearchDataSource(ServiceLocator.databaseProvider, query)

        emptySearchResultsLabel = getString(R.string.word_empty)
        presenter = EntrySearchPresenter<Word>(this, dataSource)
    }

    override fun createSearchResultsAdapter(entries: List<Word>): RecyclerView.Adapter<*> =

            WordSearchResultsAdapter(context, entries, object : WordSearchResultsAdapter.Listener {

                override fun onClickWord(word: Word) {
                    startActivity(WordDetailsActivity.createIntent(context, word))
                }
            })

    companion object {
        val ARG_QUERY = "query"

        fun create(query: String) = WordSearchFragment().apply {
            arguments = Bundle().apply { putString(ARG_QUERY, query) }
        }
    }
}
