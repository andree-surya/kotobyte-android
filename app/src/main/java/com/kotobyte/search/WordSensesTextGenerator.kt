package com.kotobyte.search

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.TextAppearanceSpan

import com.kotobyte.R
import com.kotobyte.models.Origin
import com.kotobyte.utils.SpannableTextGenerator
import com.kotobyte.models.Word


internal class WordSensesTextGenerator(context: Context) : SpannableTextGenerator<Word>(context) {

    override fun createWithBuilder(builder: SpannableStringBuilder, item: Word) {

        val senses = item.senses

        for (i in senses.indices) {
            val (text, _, extras, origins) = senses[i]

            builder.append("▸  ")

            appendBuilderWithHighlightableText(builder, text)
            appendBuilderWithExtras(builder, extras, origins)

            if (i < senses.size - 1) {
                builder.append('\n')
            }
        }
    }

    private fun appendBuilderWithExtras(
            builder: SpannableStringBuilder, extras: List<String>, origins: List<Origin>) {

        val beginMarker = " ー"
        val separator = ", "

        val extrasStartIndex = builder.length

        for (extra in extras) {

            if (builder.length == extrasStartIndex) {
                builder.append(beginMarker)

            } else {
                builder.append(separator)
            }

            builder.append(extra)
        }

        for ((language, text) in origins) {

            if (builder.length == extrasStartIndex) {
                builder.append(beginMarker)

            } else {
                builder.append(separator)
            }

            if (text == null) {
                builder.append(context.getString(
                        R.string.search_origin, language))

            } else {
                builder.append(context.getString(
                        R.string.search_origin_with_text, language, text))
            }
        }

        val extrasEndIndex = builder.length

        builder.setSpan(
                TextAppearanceSpan(context, R.style.Text_Light_Italic),
                extrasStartIndex,
                extrasEndIndex,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}
