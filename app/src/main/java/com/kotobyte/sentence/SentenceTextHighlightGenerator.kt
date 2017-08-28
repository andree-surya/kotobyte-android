package com.kotobyte.sentence

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import com.kotobyte.R
import com.kotobyte.models.Sentence
import com.kotobyte.utils.SpannableTextGenerator

class SentenceTextHighlightGenerator(context: Context) : SpannableTextGenerator<Sentence>(context) {

    override fun createWithBuilder(builder: SpannableStringBuilder, item: Sentence) {

        builder.append(item.original)

        item.tokenized.split(" ").forEach {

            val wordInHighlight = highlightPattern.findAll(it).lastOrNull()?.groupValues?.lastOrNull()
            val wordInOriginalForm = originalFormPattern.findAll(it).lastOrNull()?.groupValues?.lastOrNull()

            if (wordInHighlight != null) {

                val textToHighlight = wordInOriginalForm ?: wordInHighlight
                val locationInString = builder.indexOf(textToHighlight)

                if (locationInString >= 0) {

                    builder.setSpan(
                            BackgroundColorSpan(ContextCompat.getColor(context, R.color.highlight)),
                            locationInString,
                            locationInString + textToHighlight.length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                }
            }
        }
    }

    companion object {

        private val highlightPattern = Regex("""\{(.+?)\}""")
        private val originalFormPattern = Regex("""\[\{?(.+?)\}?\]""")
    }
}