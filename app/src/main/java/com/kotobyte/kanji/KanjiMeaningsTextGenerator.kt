package com.kotobyte.kanji

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan

import com.kotobyte.R
import com.kotobyte.models.Kanji
import com.kotobyte.utils.SpannableTextGenerator


internal class KanjiMeaningsTextGenerator(context: Context, private val shouldShowExtras: Boolean = false) : SpannableTextGenerator<Kanji>(context) {

    override fun createWithBuilder(builder: SpannableStringBuilder, item: Kanji) {

        val meanings = item.meanings
        val extras = item.extras

        for (i in meanings.indices) {

            builder.append(meanings[i])

            if (i == 0) {
                builder.setSpan(
                        StyleSpan(Typeface.BOLD),
                        0,
                        builder.length,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }

            if (i < meanings.size - 1) {
                builder.append(", ")
            }
        }

        if (shouldShowExtras) {
            val extrasStartIndex = builder.length

            for (i in extras.indices) {

                if (i == 0) {
                    builder.append(" ー")
                }

                builder.append(extras[i])

                if (i < extras.size - 1) {
                    builder.append(", ")

                } else {
                    builder.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(context, R.color.light_text)),
                            extrasStartIndex,
                            builder.length,
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }
}
