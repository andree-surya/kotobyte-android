package com.kotobyte.models.db

import java.util.Comparator

internal data class WordMatch(
        val ID: Long,
        val JSON: String,
        val highlights: String,
        private val score: Float
) {

    internal class ScoreComparator : Comparator<WordMatch> {

        override fun compare(o1: WordMatch, o2: WordMatch): Int {
            val scoreDiff = o2.score - o1.score

            if (scoreDiff > 0) {
                return -1
            }

            if (scoreDiff < 0) {
                return 1
            }

            return 0
        }
    }
}
