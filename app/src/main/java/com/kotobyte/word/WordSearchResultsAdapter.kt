package com.kotobyte.word

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.ViewGroup
import com.kotobyte.R
import com.kotobyte.databinding.ViewWordItemBinding
import com.kotobyte.models.Word
import com.kotobyte.search.EntrySearchResultsAdapter


internal class WordSearchResultsAdapter(
        private val context: Context,
        private val onClickWord: (Word) -> Unit

) : EntrySearchResultsAdapter<Word, WordSearchResultsAdapter.ViewHolder>() {

    private val literalsTextGenerator = WordLiteralsTextGenerator(context)
    private val sensesTextGenerator = WordSensesTextGenerator(context)

    private val literalsTexts = mutableMapOf<Int, SpannableString>()
    private val sensesTexts = mutableMapOf<Int, SpannableString>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_word_item, parent, false)).apply {
            binding.wordContainer.setOnClickListener { onClickWord(entries[adapterPosition]) }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.literalsTextView.text = literalsTexts[position] ?:
                literalsTextGenerator.createFrom(entries[position]).also { literalsTexts.put(position, it) }

        holder.binding.sensesTextView.text = sensesTexts[position] ?:
                sensesTextGenerator.createFrom(entries[position]).also { sensesTexts.put(position, it) }
    }

    class ViewHolder(val binding: ViewWordItemBinding) : RecyclerView.ViewHolder(binding.root)
}
