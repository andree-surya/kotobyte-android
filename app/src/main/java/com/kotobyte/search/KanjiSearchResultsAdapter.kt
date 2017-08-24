package com.kotobyte.search

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.kotobyte.R
import com.kotobyte.databinding.ViewKanjiItemBinding
import com.kotobyte.models.Kanji


internal class KanjiSearchResultsAdapter(
        private val context: Context,
        private val listener: Listener,
        private val kanjiList: List<Kanji>

) : RecyclerView.Adapter<KanjiSearchResultsAdapter.ViewHolder>() {

    private val readingsTexts: List<SpannableString>
    private val meaningsTexts: List<SpannableString>

    init {
        val readingsTextGenerator = KanjiReadingsTextGenerator(context)
        val meaningsTextGenerator = KanjiMeaningsTextGenerator(context)

        readingsTexts = kanjiList.map { readingsTextGenerator.createFrom(it) }
        meaningsTexts = kanjiList.map { meaningsTextGenerator.createFrom(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = DataBindingUtil.inflate<ViewKanjiItemBinding>(LayoutInflater.from(context), R.layout.view_kanji_item, parent, false)

        return ViewHolder(binding).apply {
            binding.kanjiContainer.setOnClickListener(OnKanjiClickListener(this))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.literalTextView.text = kanjiList[position].character.toString()
        holder.binding.readingsTextView.text = readingsTexts[position]
        holder.binding.meaningsTextView.text = meaningsTexts[position]
    }

    override fun getItemCount(): Int = kanjiList.size

    internal interface Listener {
        fun onClickKanji(position: Int, kanji: Kanji)
    }

    internal class ViewHolder(val binding: ViewKanjiItemBinding) : RecyclerView.ViewHolder(binding.root)

    private inner class OnKanjiClickListener(private val viewHolder: ViewHolder) : View.OnClickListener {

        override fun onClick(v: View) {
            val position = viewHolder.adapterPosition

            listener.onClickKanji(position, kanjiList[position])
        }
    }
}
