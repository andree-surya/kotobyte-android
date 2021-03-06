package com.kotobyte.sentence

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import com.kotobyte.R
import com.kotobyte.models.Sentence
import com.kotobyte.utils.SpannableTextGenerator

class SentenceTextGenerator(
        context: Context,
        private val onClickToken: (Sentence.Token) -> Unit

) : SpannableTextGenerator<Sentence>(context) {

    override fun createWithBuilder(builder: SpannableStringBuilder, item: Sentence) {

        builder.append(item.original)

        item.tokens.forEach { token ->

            builder.setSpan(
                    ClickableTokenSpan(context, token, onClickToken),
                    token.location.start,
                    token.location.endInclusive + 1,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )

            if (token.highlighted) {

                builder.setSpan(
                        BackgroundColorSpan(ContextCompat.getColor(context, R.color.highlight)),
                        token.location.start,
                        token.location.endInclusive + 1,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
}