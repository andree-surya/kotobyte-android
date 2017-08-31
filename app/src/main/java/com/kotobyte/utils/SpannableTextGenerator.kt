package com.kotobyte.utils

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan

import com.kotobyte.R

abstract class SpannableTextGenerator<in T> protected constructor(protected val context: Context) {

    private val spannableStringBuilder = SpannableStringBuilder()

    fun createFrom(item: T): SpannableString {

        createWithBuilder(spannableStringBuilder, item)

        val spannableText = SpannableString.valueOf(spannableStringBuilder)

        spannableStringBuilder.clearSpans()
        spannableStringBuilder.clear()

        return spannableText
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

    protected abstract fun createWithBuilder(builder: SpannableStringBuilder, item: T)

    companion object {
        private val HIGHLIGHT_START = '{'
        private val HIGHLIGHT_END = '}'
    }
}
