package com.kotobyte.sentence

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.kotobyte.R
import com.kotobyte.models.Sentence

class ClickableTokenSpan(
        private val context: Context,
        private val token: Sentence.Token,
        private val callback: (Sentence.Token) -> Unit

) : ClickableSpan() {

    override fun updateDrawState(ds: TextPaint?) {

        ds?.color = ContextCompat.getColor(context, R.color.primary_text)
        ds?.isUnderlineText = false
    }

    override fun onClick(widget: View?) {
        callback(token)
    }
}