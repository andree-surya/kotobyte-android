package com.kotobyte.base

import com.kotobyte.models.Kanji
import com.kotobyte.models.Sentence
import com.kotobyte.models.Word

interface DatabaseConnection {

    fun searchWords(query: String): List<Word>
    fun searchKanji(queries: List<String>): List<Kanji>
    fun searchSentences(queries: List<String>): List<Sentence>
}
