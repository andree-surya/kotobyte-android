package com.kotobyte.search

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.kotobyte.R
import com.kotobyte.databinding.ViewKanjiItemBinding
import com.kotobyte.models.Kanji


internal class KanjiSearchResultsAdapter(
        private val context: Context,
        private val listener: Listener,
        private val kanjiSearchResults: List<Kanji>

) : RecyclerView.Adapter<KanjiSearchResultsAdapter.ViewHolder>() {

    private val readingsTextGenerator: KanjiReadingsTextGenerator = KanjiReadingsTextGenerator(context, kanjiSearchResults)
    private val meaningsTextGenerator: KanjiMeaningsTextGenerator = KanjiMeaningsTextGenerator(context, kanjiSearchResults)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = DataBindingUtil.inflate<ViewKanjiItemBinding>(LayoutInflater.from(context), R.layout.view_kanji_item, parent, false)

        return ViewHolder(binding).apply {
            binding.kanjiContainer.setOnClickListener(OnKanjiClickListener(this))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.literalTextView.text = kanjiSearchResults[position].character.toString()
        holder.binding.readingsTextView.text = readingsTextGenerator.getSpannableString(position)
        holder.binding.meaningsTextView.text = meaningsTextGenerator.getSpannableString(position)
    }

    override fun getItemCount(): Int = kanjiSearchResults.size

    internal interface Listener {
        fun onClickKanji(position: Int, kanji: Kanji)
    }

    internal class ViewHolder(val binding: ViewKanjiItemBinding) : RecyclerView.ViewHolder(binding.root)

    private inner class OnKanjiClickListener(private val viewHolder: ViewHolder) : View.OnClickListener {

        override fun onClick(v: View) {
            val position = viewHolder.adapterPosition

            listener.onClickKanji(position, kanjiSearchResults[position])
        }
    }
}
