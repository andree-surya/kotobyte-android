package com.kotobyte.word

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.kotobyte.R
import com.kotobyte.kanji.KanjiSearchFragment
import com.kotobyte.models.Word
import com.kotobyte.sentence.SentenceSearchFragment

class WordDetailsPageAdapter(val word: Word, val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount() = 2

    override fun getPageTitle(position: Int): String = when(position) {

        0 -> context.getString(R.string.word_sentences)
        1 -> context.getString(R.string.word_kanji)

        else -> throwUnknownPageError(position)
    }

    override fun getItem(position: Int): Fragment = when(position) {

        0 -> SentenceSearchFragment.create((word.literals + word.readings).map { it.text })
        1 -> KanjiSearchFragment.create(word.literals.map { it.text })

        else -> throwUnknownPageError(position)
    }

    private fun throwUnknownPageError(position: Int): Nothing {
        throw IllegalArgumentException("Unknown page: $position")
    }
}