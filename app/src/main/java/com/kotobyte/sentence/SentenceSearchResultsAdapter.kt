package com.kotobyte.sentence

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import com.kotobyte.R
import com.kotobyte.databinding.ViewSentenceItemBinding
import com.kotobyte.models.Sentence

class SentenceSearchResultsAdapter(
        private val context: Context,
        private val sentences: List<Sentence>,
        private val listener: Listener

) : RecyclerView.Adapter<SentenceSearchResultsAdapter.ViewHolder>() {

    private val textHighlightGenerator = SentenceTextHighlightGenerator(context)
    private val highlightedTexts = SparseArray<SpannableString>(sentences.size)

    override fun getItemCount() = sentences.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =

            ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_sentence_item, parent, false)).apply {
                binding.sentenceContainer.setOnClickListener({ listener.onClickSentence(sentences[adapterPosition]) })
            }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.binding.originalTextView.text = highlightedTexts[position] ?:
                textHighlightGenerator.createFrom(sentences[position]).also { highlightedTexts.put(position, it) }

        holder.binding.translatedTextView.text = sentences[position].translated
    }

    interface Listener {
        fun onClickSentence(sentence: Sentence)
    }

    class ViewHolder(val binding: ViewSentenceItemBinding) : RecyclerView.ViewHolder(binding.root)
}