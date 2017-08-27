package com.kotobyte.kanji

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import com.kotobyte.R
import com.kotobyte.databinding.ViewKanjiItemBinding
import com.kotobyte.models.Kanji


class KanjiSearchResultsAdapter(
        private val context: Context,
        private val kanjiList: List<Kanji>,
        private val listener: Listener

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
            binding.kanjiContainer.setOnClickListener({ listener.onClickKanji(kanjiList[adapterPosition]) })
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.literalTextView.text = kanjiList[position].character.toString()
        holder.binding.readingsTextView.text = readingsTexts[position]
        holder.binding.meaningsTextView.text = meaningsTexts[position]
    }

    override fun getItemCount(): Int = kanjiList.size

    interface Listener {
        fun onClickKanji(kanji: Kanji)
    }

    class ViewHolder(val binding: ViewKanjiItemBinding) : RecyclerView.ViewHolder(binding.root)
}
