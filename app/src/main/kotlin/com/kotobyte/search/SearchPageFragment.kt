package com.kotobyte.search

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.kotobyte.R
import com.kotobyte.base.ServiceLocator
import com.kotobyte.databinding.FragmentSearchPageBinding
import com.kotobyte.models.Kanji
import com.kotobyte.models.Word
import com.kotobyte.utils.ErrorDialogFragment

class SearchPageFragment : Fragment(), SearchPageContracts.View {

    private var binding: FragmentSearchPageBinding? = null
    private var presenter: SearchPageContracts.Presenter? = null
    private var searchResultsAdapter: SearchResultsAdapter? = null

    val query: String
        get() = arguments?.getString(ARG_QUERY) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = SearchPagePresenter(this, SearchPageDataSource(ServiceLocator.databaseProvider!!), query)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_search_page, container, false)

        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        presenter?.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter?.onDestroy()
    }

    override fun showWordSearchProgressBar(show: Boolean) {
        binding?.progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showWordSearchResultsView(show: Boolean) {
        binding?.searchResultsView?.animate()?.alpha(if (show) 1f else 0f)?.start()
    }

    override fun showWordSearchResults(words: List<Word>) {
        searchResultsAdapter = SearchResultsAdapter(context, mWordSearchResultsAdapterListener, words)

        binding?.searchResultsView?.adapter = searchResultsAdapter
    }

    override fun showNoWordSearchResultsLabel(show: Boolean) {
        binding?.noSearchResultsLabel?.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showKanjiSearchResults(position: Int, kanjiList: List<Kanji>) {
        searchResultsAdapter?.setKanjiSearchResults(position, kanjiList)
    }

    override fun showKanjiDetailScreen(kanji: Kanji) =
            KanjiDetailDialogFragment.create(kanji).show(childFragmentManager, KanjiDetailDialogFragment::class.java.simpleName)

    override fun showUnknownError(error: Throwable) {
        Log.e(TAG, error.localizedMessage, error)

        ErrorDialogFragment.create().show(fragmentManager, ErrorDialogFragment::class.java.simpleName)
    }

    private val mWordSearchResultsAdapterListener = object : SearchResultsAdapter.Listener {

        override fun onRequestKanjiListForWord(position: Int, word: Word) {
            presenter?.onRequestKanjiListForWord(position, word)
        }

        override fun onRequestDetailForKanji(kanji: Kanji) {
            presenter?.onRequestDetailForKanji(kanji)
        }
    }

    companion object {
        private val TAG = SearchPageFragment::class.java.simpleName

        private val ARG_QUERY = "query"

        fun create(query: CharSequence) = SearchPageFragment().apply {

            arguments = Bundle().apply {
                putString(ARG_QUERY, query.toString())
            }
        }
    }
}
