package com.kotobyte.search

import android.content.Context
import android.text.SpannableStringBuilder

import com.kotobyte.utils.SpannableTextGenerator
import com.kotobyte.models.Kanji


internal class KanjiReadingsTextGenerator(context: Context) : SpannableTextGenerator<Kanji>(context) {

    override fun createWithBuilder(builder: SpannableStringBuilder, item: Kanji) {

        val readings = item.readings

        for (i in readings.indices) {

            builder.append(readings[i])

            if (i < readings.size - 1) {
                builder.append('、')
            }
        }
    }
}
