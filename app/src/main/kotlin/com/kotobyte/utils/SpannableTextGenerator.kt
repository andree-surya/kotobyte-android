package com.kotobyte.utils

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.util.SparseArray

import com.kotobyte.R

abstract class SpannableTextGenerator protected constructor(protected val context: Context, initialCapacity: Int) {

    private val spannableStrings = SparseArray<SpannableString>(initialCapacity)
    private val spannableStringBuilder = SpannableStringBuilder()

    val spannableString: SpannableString
        get() = getSpannableString(0)

    fun getSpannableString(position: Int): SpannableString {

        var spannableString = spannableStrings.get(position)

        if (spannableString == null) {
            createSpannableWithBuilder(spannableStringBuilder, position)
            spannableString = SpannableString.valueOf(spannableStringBuilder)

            spannableStringBuilder.clearSpans()
            spannableStringBuilder.clear()

            spannableStrings.put(position, spannableString)
        }

        return spannableString
    }

    protected fun appendBuilderWithHighlightableText(builder: SpannableStringBuilder, text: CharSequence) {

        var startHighlightIndex = -1

        for (j in 0 until text.length) {
            val character = text[j]

            if (character == HIGHLIGHT_START) {
                startHighlightIndex = builder.length

            } else if (character == HIGHLIGHT_END && startHighlightIndex >= 0) {
                val endHighlightIndex = builder.length

                builder.setSpan(
                        BackgroundColorSpan(ContextCompat.getColor(context, R.color.highlight)),
                        startHighlightIndex,
                        endHighlightIndex,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

                startHighlightIndex = -1

            } else {
                builder.append(character)
            }
        }
    }

    protected abstract fun createSpannableWithBuilder(builder: SpannableStringBuilder, position: Int)

    companion object {
        private val HIGHLIGHT_START = '{'
        private val HIGHLIGHT_END = '}'
    }
}
