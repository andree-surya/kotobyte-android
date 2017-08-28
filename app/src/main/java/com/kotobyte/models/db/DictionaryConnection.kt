package com.kotobyte.models.db

import android.database.Cursor

import com.kotobyte.base.DatabaseConnection
import com.kotobyte.models.Kanji
import com.kotobyte.models.Sentence
import com.kotobyte.models.Word
import com.moji4j.MojiConverter
import com.moji4j.MojiDetector

import org.sqlite.database.sqlite.SQLiteDatabase

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

internal class DictionaryConnection(name: String, version: Int) : DatabaseConnection, AutoCloseable {

    private val database: SQLiteDatabase = SQLiteDatabase.openDatabase(name, null, SQLiteDatabase.OPEN_READWRITE).apply {
        this.version = version
    }

    private val mojiConverter = MojiConverter()
    private val mojiDetector = MojiDetector()

    private var labelsMap = mapOf<String, String>()
    private var languagesMap = mapOf<String, String>()
    private var JLPTMap = mapOf<String, String>()
    private var gradesMap = mapOf<String, String>()

    override fun close() = database.close()

    override fun searchWords(query: String): List<Word> {

        val wordMatches = ArrayList<WordMatch>()

        if (labelsMap.isEmpty()) {
            labelsMap = readStringMapFromCursor(database.rawQuery(SELECT_LABELS_SQL, null))
        }

        if (languagesMap.isEmpty()) {
            languagesMap = readStringMapFromCursor(database.rawQuery(SELECT_LANGUAGES_SQL, null))
        }

        if (mojiDetector.hasKanji(query) || mojiDetector.hasKana(query)) {
            wordMatches.addAll(searchWordsByLiterals(query, WORD_SEARCH_LIMIT))

        } else if (mojiDetector.hasRomaji(query)) {
            wordMatches.addAll(searchWordsBySenses(query, WORD_SEARCH_LIMIT))

            // Not enough search results in English. User query is probably a romanized Japanese.
            if (wordMatches.size < ROMAJI_SEARCH_THRESHOLD) {

                val romajiSearchLimit = WORD_SEARCH_LIMIT - wordMatches.size
                wordMatches.addAll(searchWordsByRomaji(query, romajiSearchLimit))
            }
        }

        return convertWordMatchesToWords(wordMatches)
    }

    override fun searchKanji(queries: List<String>): List<Kanji> {

        val sanitizedQuery = queries.joinToString(separator = "") { it.filter { mojiDetector.isKanji(it) } }
        val finalQueryString = sanitizedQuery.toCharArray().joinToString(separator = " OR ")

        if (finalQueryString.isNotEmpty()) {

            if (JLPTMap.isEmpty()) {
                JLPTMap = readStringMapFromCursor(database.rawQuery(SELECT_JLPT_SQL, null))
            }

            if (gradesMap.isEmpty()) {
                gradesMap = readStringMapFromCursor(database.rawQuery(SELECT_GRADES_SQL, null))
            }

            val cursor = database.rawQuery(SEARCH_KANJI_SQL,
                    arrayOf(finalQueryString, KANJI_SEARCH_LIMIT.toString()))

            return readKanjiListFromCursor(cursor)
        }

        return emptyList()
    }

    override fun searchSentences(queries: List<String>): List<Sentence> {

        val queryString = queries.joinToString(separator = " OR ") { "\"$it\"" }

        val cursor = database.rawQuery(SEARCH_SENTENCES_SQL,
                arrayOf(queryString, SENTENCE_SEARCH_LIMIT.toString()))

        return readSentencesFromCursor(cursor)
    }

    private fun searchWordsByLiterals(query: String, limit: Int): List<WordMatch> {

        val sanitizedQuery= query.replace("\\p{Blank}".toRegex(), "")
        val queryBuilder = StringBuilder(sanitizedQuery)

        for (i in sanitizedQuery.length downTo 1) {

            queryBuilder.append(" OR ")
            queryBuilder.append(sanitizedQuery.substring(0, i))
            queryBuilder.append('*')
        }

        if (queryBuilder.isNotEmpty()) {

            val cursor = database.rawQuery(SEARCH_LITERALS_SQL,
                    arrayOf(queryBuilder.toString(), limit.toString()))

            return readWordMatchesFromCursor(cursor)
        }

        return emptyList()
    }

    private fun searchWordsBySenses(query: String, limit: Int): List<WordMatch> {

        val sanitizedQuery = query.trim { it <= ' ' }

        if (sanitizedQuery.isNotEmpty()) {

            val cursor = database.rawQuery(SEARCH_SENSES_SQL,
                    arrayOf(sanitizedQuery, limit.toString()))

            return readWordMatchesFromCursor(cursor)
        }

        return emptyList()
    }

    private fun searchWordsByRomaji(query: String, limit: Int): List<WordMatch> {
        val wordMatches = mutableListOf<WordMatch>()

        val hiragana = mojiConverter.convertRomajiToHiragana(query)
        wordMatches.addAll(searchWordsByLiterals(hiragana, limit / 2))

        val katakana = mojiConverter.convertRomajiToKatakana(query)
        wordMatches.addAll(searchWordsByLiterals(katakana, limit / 2))

        Collections.sort(wordMatches, WordMatch.ScoreComparator())

        return wordMatches
    }

    private fun readKanjiListFromCursor(cursor: Cursor): List<Kanji> {

        val kanjiList = mutableListOf<Kanji>()
        val kanjiEntryDecoder = KanjiEntryDecoder(JLPTMap, gradesMap)

        while (cursor.moveToNext()) {

            val kanji = kanjiEntryDecoder.decode(
                    cursor.getLong(0),
                    cursor.getString(1))

            kanjiList.add(kanji)
        }

        cursor.close()

        return kanjiList
    }

    private fun readSentencesFromCursor(cursor: Cursor): List<Sentence> {

        val sentences = mutableListOf<Sentence>()

        while (cursor.moveToNext()) {
            sentences.add(Sentence(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
        }

        return sentences
    }

    private fun convertWordMatchesToWords(wordMatches: List<WordMatch>): List<Word> {

        val words = mutableListOf<Word>()
        val wordEntryDecoder = WordEntryDecoder(labelsMap, languagesMap)

        for ((ID, JSON, highlights) in wordMatches) {
            words.add(wordEntryDecoder.decode(ID, JSON, highlights))
        }

        return words
    }

    private fun readWordMatchesFromCursor(cursor: Cursor): List<WordMatch> {

        val wordMatches = mutableListOf<WordMatch>()

        while (cursor.moveToNext()) {

            val wordMatch = WordMatch(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getFloat(3))

            wordMatches.add(wordMatch)
        }

        cursor.close()

        return wordMatches
    }

    private fun readStringMapFromCursor(cursor: Cursor): Map<String, String> {

        val map = HashMap<String, String>()

        while (cursor.moveToNext()) {
            map.put(cursor.getString(0), cursor.getString(1))
        }

        cursor.close()

        return map
    }

    companion object {

        init {
            System.loadLibrary("sqliteX")
        }

        private val KANJI_SEARCH_LIMIT = 10
        private val WORD_SEARCH_LIMIT = 50
        private val SENTENCE_SEARCH_LIMIT = 20
        private val ROMAJI_SEARCH_THRESHOLD = 10

        private val SEARCH_WORDS_SQL = "" +
                "with search_results as (%s)\n" +
                "    select id, json, group_concat(highlight, ';') highlights, min(score) score\n" +
                "    from words join search_results on (id = word_id) group by id order by score;"

        private val SEARCH_LITERALS_SQL = String.format(SEARCH_WORDS_SQL,
                "select word_id, highlight(literals_fts, 0, '{', '}') highlight, rank * priority score from literals_fts(?) order by score limit ?")

        private val SEARCH_SENSES_SQL = String.format(SEARCH_WORDS_SQL,
                "select word_id, highlight(senses_fts, 0, '{', '}') highlight, rank score from senses_fts(?) order by score limit ?")

        private val SEARCH_KANJI_SQL =
                "select id, json from kanji join kanji_fts(?) on (id = kanji_id) order by rank limit ?;"

        private val SEARCH_SENTENCES_SQL =
                "select original, highlight(sentences_fts, 0, '{', '}') highlight, translated from sentences join sentences_fts(?) on (id = sentence_id) order by rank limit ?;"

        private val SELECT_LABELS_SQL = "select code, text from labels;"
        private val SELECT_LANGUAGES_SQL = "select code, text from languages;"
        private val SELECT_JLPT_SQL = "select number, text from jlpt;"
        private val SELECT_GRADES_SQL = "select number, text from grades;"
    }
}
