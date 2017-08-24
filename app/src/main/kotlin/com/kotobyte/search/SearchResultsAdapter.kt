package com.kotobyte.search

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.kotobyte.R
import com.kotobyte.databinding.ViewWordItemCollapsedBinding
import com.kotobyte.databinding.ViewWordItemExpandedBinding
import com.kotobyte.models.Kanji
import com.kotobyte.models.Word


internal class SearchResultsAdapter(
        private val context: Context,
        private val listener: Listener,
        private val wordSearchResults: List<Word>

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var expandedItemPosition = NONE
    private val kanjiSearchResultsAdapter = SparseArray<KanjiSearchResultsAdapter>()

    private val mLiteralsTextGenerator = WordLiteralsTextGenerator(context, wordSearchResults)
    private val mSensesTextGenerator = WordSensesTextGenerator(context, wordSearchResults)

    private val kanjiAdapterListener = object : KanjiSearchResultsAdapter.Listener {

        override fun onClickKanji(position: Int, kanji: Kanji) =
                listener.onRequestDetailForKanji(kanji)
    }

    init {
        setHasStableIds(true)
    }

    fun setKanjiSearchResults(position: Int, kanjiSearchResults: List<Kanji>) {

        kanjiSearchResultsAdapter.put(position,
                KanjiSearchResultsAdapter(context, kanjiAdapterListener, kanjiSearchResults))

        notifyItemChanged(position)
    }

    override fun getItemCount(): Int = wordSearchResults.size

    override fun getItemId(position: Int): Long = wordSearchResults[position].ID

    override fun getItemViewType(position: Int): Int =
            if (expandedItemPosition == position) EXPANDED_ITEM else COLLAPSED_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if (viewType == EXPANDED_ITEM) {

            val binding = DataBindingUtil.inflate<ViewWordItemExpandedBinding>(
                    LayoutInflater.from(parent.context), R.layout.view_word_item_expanded, parent, false)

            return ExpandedViewHolder(binding).apply {
                binding.wordItem.wordContainer.setOnClickListener(OnWordClickListener(this))
            }

        } else {
            val binding = DataBindingUtil.inflate<ViewWordItemCollapsedBinding>(
                    LayoutInflater.from(parent.context), R.layout.view_word_item_collapsed, parent, false)

            return CollapsedViewHolder(binding).apply {
                binding.wordItem.wordContainer.setOnClickListener(OnWordClickListener(this))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (getItemViewType(position) == EXPANDED_ITEM) {
            val viewHolder = holder as ExpandedViewHolder

            viewHolder.binding.wordItem.literalsTextView.text = mLiteralsTextGenerator.getSpannableString(position)
            viewHolder.binding.wordItem.sensesTextView.text = mSensesTextGenerator.getSpannableString(position)

            viewHolder.binding.progressBar.visibility = View.GONE
            viewHolder.binding.kanjiListView.visibility = View.GONE

            val kanjiSearchResultsAdapter = kanjiSearchResultsAdapter.get(position)

            if (kanjiSearchResultsAdapter == null) {
                viewHolder.binding.progressBar.visibility = View.VISIBLE

            } else {
                viewHolder.binding.kanjiListView.visibility = View.VISIBLE
                viewHolder.binding.kanjiListView.adapter = kanjiSearchResultsAdapter
            }

        } else {
            val viewHolder = holder as CollapsedViewHolder

            viewHolder.binding.wordItem.literalsTextView.text = mLiteralsTextGenerator.getSpannableString(position)
            viewHolder.binding.wordItem.sensesTextView.text = mSensesTextGenerator.getSpannableString(position)
        }
    }

    internal interface Listener {
        fun onRequestKanjiListForWord(position: Int, word: Word)
        fun onRequestDetailForKanji(kanji: Kanji)
    }

    private class ExpandedViewHolder(val binding: ViewWordItemExpandedBinding) : RecyclerView.ViewHolder(binding.root)
    private class CollapsedViewHolder(val binding: ViewWordItemCollapsedBinding) : RecyclerView.ViewHolder(binding.root)

    private inner class OnWordClickListener internal constructor(private val mViewHolder: RecyclerView.ViewHolder) : View.OnClickListener {

        override fun onClick(v: View) {
            val position = mViewHolder.adapterPosition

            if (expandedItemPosition == position) {
                expandedItemPosition = NONE

                notifyItemChanged(position)

            } else {
                val lastExpandedItemPosition = expandedItemPosition

                expandedItemPosition = position

                if (lastExpandedItemPosition != NONE) {
                    notifyItemChanged(lastExpandedItemPosition)
                }

                notifyItemChanged(expandedItemPosition)

                if (kanjiSearchResultsAdapter.get(position) == null) {
                    listener.onRequestKanjiListForWord(position, wordSearchResults[position])
                }
            }
        }
    }

    companion object {

        private val COLLAPSED_ITEM = 1
        private val EXPANDED_ITEM = 2
        private val NONE = -1
    }
}
