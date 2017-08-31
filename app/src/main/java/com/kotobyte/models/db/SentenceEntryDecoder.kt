package com.kotobyte.models.db

import com.kotobyte.models.Sentence

internal class SentenceEntryDecoder {

    private val highlightPattern = Regex("""[{}]""") // Match "{" and "}" in "{結局}"
    private val lemmaFormPattern = Regex("""^([^(\[]+)""") // Match "為る" in "為る(する)[した]"
    private val originalFormPattern = Regex("""\[(.+)\]""") // Match "おおいに" in "大いに[おおいに]"

    fun decode(ID: Long, originalText: String, translatedText: String, tokenizedText: String): Sentence {

        val rawTokens = tokenizedText.split(" ")
        val tokens = mutableListOf<Sentence.Token>()

        var searchOffset = 0

        rawTokens.forEach { rawToken ->

            val lemma = (lemmaFormPattern.find(rawToken)?.groupValues?.last() ?: rawToken).replace(highlightPattern, "")
            val text = (originalFormPattern.find(rawToken)?.groupValues?.last() ?: lemma).replace(highlightPattern, "")

            val positionInOriginalText = originalText.indexOf(text, searchOffset)

            if (positionInOriginalText >= 0) {
                searchOffset = positionInOriginalText + text.length

                val location = positionInOriginalText until searchOffset
                val highlighted = rawToken.contains("{")

                tokens.add(Sentence.Token(text, lemma, location, highlighted))
            }
        }

        return Sentence(ID, originalText, translatedText, tokens)
    }
}