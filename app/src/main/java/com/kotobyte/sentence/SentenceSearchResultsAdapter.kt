package com.kotobyte.sentence

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import com.kotobyte.R
import com.kotobyte.databinding.ViewSentenceItemBinding
import com.kotobyte.models.Sentence
import com.kotobyte.search.EntrySearchResultsAdapter

class SentenceSearchResultsAdapter(
        private val context: Context,
        onClickToken: (Sentence.Token) -> Unit

) : EntrySearchResultsAdapter<Sentence, SentenceSearchResultsAdapter.ViewHolder>() {

    private val highlightedTextGenerator = SentenceTextGenerator(context, onClickToken)
    private val highlightedTexts = mutableMapOf<Int, SpannableString>()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) =
        ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_sentence_item, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val sentence = entries[position]

        holder.binding.originalTextView.text = highlightedTexts[position] ?:
                highlightedTextGenerator.createFrom(sentence).also { highlightedTexts.put(position, it) }

        holder.binding.translatedTextView.text = sentence.translated
    }

    class ViewHolder(val binding: ViewSentenceItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.originalTextView.movementMethod = LinkMovementMethod()
        }
    }
}