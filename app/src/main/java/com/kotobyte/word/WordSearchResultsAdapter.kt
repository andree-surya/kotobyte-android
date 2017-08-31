package com.kotobyte.word

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import com.kotobyte.R
import com.kotobyte.databinding.ViewWordItemBinding
import com.kotobyte.models.Word


internal class WordSearchResultsAdapter(
        private val context: Context,
        private val words: List<Word>,
        private val onClickWord: (Word) -> Unit

) : RecyclerView.Adapter<WordSearchResultsAdapter.ViewHolder>() {

    private val literalsTextGenerator = WordLiteralsTextGenerator(context)
    private val sensesTextGenerator = WordSensesTextGenerator(context)

    private val literalsTexts = SparseArray<SpannableString>(words.size)
    private val sensesTexts = SparseArray<SpannableString>(words.size)

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int = words.size

    override fun getItemId(position: Int): Long = words[position].ID

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {

        val binding = DataBindingUtil.inflate<ViewWordItemBinding>(
                LayoutInflater.from(context), R.layout.view_word_item, parent, false)

        return ViewHolder(binding).apply {
            binding.wordContainer.setOnClickListener { onClickWord(words[adapterPosition]) }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.literalsTextView.text = literalsTexts.get(position) ?:
                literalsTextGenerator.createFrom(words[position]).also { literalsTexts.put(position, it) }

        holder.binding.sensesTextView.text = sensesTexts.get(position) ?:
                sensesTextGenerator.createFrom(words[position]).also { sensesTexts.put(position, it) }
    }

    class ViewHolder(val binding: ViewWordItemBinding) : RecyclerView.ViewHolder(binding.root)
}
