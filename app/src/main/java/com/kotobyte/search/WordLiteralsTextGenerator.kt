package com.kotobyte.search

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan

import com.kotobyte.R
import com.kotobyte.utils.SpannableTextGenerator
import com.kotobyte.models.Literal
import com.kotobyte.models.Word


internal class WordLiteralsTextGenerator(context: Context, private val words: List<Word>) : SpannableTextGenerator(context, words.size) {

    override fun createSpannableWithBuilder(builder: SpannableStringBuilder, position: Int) {

        val readings = words[position].readings
        val literals = words[position].literals

        for (i in readings.indices) {
            val reading = readings[i]

            appendBuilderWithLiteral(builder, reading)

            if (i < readings.size - 1) {
                builder.append('、')
            }
        }

        for (i in literals.indices) {
            val wordLiteral = literals[i]

            if (i == 0) {
                builder.append('【')
            }

            appendBuilderWithLiteral(builder, wordLiteral)
            builder.append(if (i < literals.size - 1) '、' else '】')
        }
    }

    private fun appendBuilderWithLiteral(builder: SpannableStringBuilder, wordLiteral: Literal) {

        val literalStartIndex = builder.length

        appendBuilderWithHighlightableText(builder, wordLiteral.text)

        val literalEndIndex = builder.length

        if (wordLiteral.priority === Literal.Priority.LOW) {

            builder.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.light_text)),
                    literalStartIndex,
                    literalEndIndex,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    }
}
