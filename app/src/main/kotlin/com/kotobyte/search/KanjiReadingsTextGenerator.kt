package com.kotobyte.search

import android.content.Context
import android.text.SpannableStringBuilder

import com.kotobyte.utils.SpannableTextGenerator
import com.kotobyte.models.Kanji


internal class KanjiReadingsTextGenerator(context: Context, private val kanjiList: List<Kanji>) : SpannableTextGenerator(context, kanjiList.size) {

    constructor(context: Context, kanji: Kanji) : this(context, listOf<Kanji>(kanji))

    override fun createSpannableWithBuilder(builder: SpannableStringBuilder, position: Int) {

        val readings = kanjiList[position].readings

        for (i in readings.indices) {

            builder.append(readings[i])

            if (i < readings.size - 1) {
                builder.append('、')
            }
        }
    }
}
