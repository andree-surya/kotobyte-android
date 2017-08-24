package com.kotobyte.base

import com.kotobyte.models.Kanji
import com.kotobyte.models.Word

interface DatabaseConnection {

    fun searchWords(query: String): List<Word>
    fun searchKanji(query: String): List<Kanji>
}
