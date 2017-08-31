package com.kotobyte.models

data class Sentence(
        val ID: Long,
        val original: String,
        val translated: String,
        val tokens: List<Token>
) {

    data class Token(
            val text: String,
            val lemma: String,
            val location: IntRange,
            val highlighted: Boolean
    )
}