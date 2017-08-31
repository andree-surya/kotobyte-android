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
import com.kotobyte.search.EntrySearchResultsAdapter


class KanjiSearchResultsAdapter(
        private val context: Context,
        private val onClickKanji: (Kanji) -> Unit

) : EntrySearchResultsAdapter<Kanji, KanjiSearchResultsAdapter.ViewHolder>() {

    private val readingsTextGenerator = KanjiReadingsTextGenerator(context)
    private val meaningsTextGenerator = KanjiMeaningsTextGenerator(context)

    private val readingsTexts = mutableMapOf<Int, SpannableString>()
    private val meaningsTexts = mutableMapOf<Int, SpannableString>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_kanji_item, parent, false)).apply {
            binding.kanjiContainer.setOnClickListener({ onClickKanji(entries[adapterPosition]) })
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val kanji = entries[position]

        holder.binding.literalTextView.text = kanji.character.toString()

        holder.binding.readingsTextView.text = readingsTexts[position] ?:
                readingsTextGenerator.createFrom(kanji).also { readingsTexts[position] = it }

        holder.binding.meaningsTextView.text = meaningsTexts[position] ?:
                meaningsTextGenerator.createFrom(kanji).also { meaningsTexts[position] = it }
    }

    class ViewHolder(val binding: ViewKanjiItemBinding) : RecyclerView.ViewHolder(binding.root)
}
