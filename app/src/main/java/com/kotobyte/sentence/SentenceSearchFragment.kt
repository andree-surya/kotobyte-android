package com.kotobyte.sentence

import android.os.Bundle
import android.support.v4.app.Fragment

class SentenceSearchFragment : Fragment() {

    companion object {
        private val TAG = SentenceSearchFragment::class.java.simpleName

        private val ARG_QUERIES = "queries"

        fun create(queries: Array<CharSequence>) = SentenceSearchFragment().apply {

            arguments = Bundle().apply {
                putCharSequenceArray(ARG_QUERIES, queries)
            }
        }
    }
}