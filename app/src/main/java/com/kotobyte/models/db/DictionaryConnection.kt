package com.kotobyte.models.db

import com.kotobyte.base.DatabaseConnection
import com.kotobyte.models.Kanji
import com.kotobyte.models.Sentence
import com.kotobyte.models.Word
import com.moji4j.MojiConverter
import com.moji4j.MojiDetector
import org.sqlite.database.sqlite.SQLiteDatabase


internal class DictionaryConnection(name: String, version: Int) : DatabaseConnection, AutoCloseable {

    private val database: SQLiteDatabase = SQLiteDatabase.openDatabase(name, null, SQLiteDatabase.OPEN_READWRITE).apply {
        this.version = version
    }

    private val mojiConverter = MojiConverter()
    private val mojiDetector = MojiDetector()

    private val wordEntryDecoder by lazy { WordEntryDecoder(labelsMap, languagesMap) }
    private val kanjiEntryDecoder by lazy { KanjiEntryDecoder(JLPTMap, gradesMap) }
    private val sentenceEntryDecoder by lazy { SentenceEntryDecoder() }

    private val labelsMap: Map<String, String> by lazy { readStringMap("select code, text from labels") }
    private val languagesMap: Map<String, String> by lazy { readStringMap("select code, text from languages") }
    private val JLPTMap: Map<String, String> by lazy { readStringMap("select number, text from jlpt") }
    private val gradesMap: Map<String, String> by lazy { readStringMap("select number, text from grades") }

    override fun close() = database.close()

    override fun searchWords(query: String): List<Word> {

        val wordMatches = ArrayList<WordMatch>()

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

        return wordMatches.map { wordEntryDecoder.decode(it.ID, it.JSON, it.highlights) }
    }

    override fun searchKanji(queries: List<String>): List<Kanji> {

        val searchParameter = queries.joinToString("").toCharArray().joinToString(" OR ")

        if (searchParameter.isNotEmpty()) {
            val kanjiList = mutableListOf<Kanji>()

            executeQuery(SEARCH_KANJI_SQL, searchParameter, KANJI_SEARCH_LIMIT.toString()).use {

                while (it.moveToNext()) {

                    kanjiList.add(kanjiEntryDecoder.decode(
                            it.getLong(0),
                            it.getString(1)
                    ))
                }
            }

            return kanjiList
        }

        return emptyList()
    }

    override fun searchSentences(queries: List<String>): List<Sentence> {

        val searchParameter = queries.joinToString(" OR ") { "\"$it\"" }
        val sentences = mutableListOf<Sentence>()

        executeQuery(SEARCH_SENTENCES_SQL, searchParameter, SENTENCE_SEARCH_LIMIT.toString()).use {

            while (it.moveToNext()) {

                sentences.add(sentenceEntryDecoder.decode(
                        it.getLong(0),
                        it.getString(1),
                        it.getString(2),
                        it.getString(3)
                ))
            }
        }

        return sentences
    }

    private fun searchWordsByLiterals(query: String, limit: Int): List<WordMatch> {

        val sanitizedSearchParameter = query.replace("\\p{Blank}".toRegex(), "")
        val searchParameterBuilder = StringBuilder(sanitizedSearchParameter)

        for (i in sanitizedSearchParameter.length downTo 1) {

            searchParameterBuilder.append(" OR ")
            searchParameterBuilder.append(sanitizedSearchParameter.substring(0, i))
            searchParameterBuilder.append('*')
        }

        if (searchParameterBuilder.isNotEmpty()) {
            return queryWordMatches(SEARCH_LITERALS_SQL, searchParameterBuilder.toString(), limit.toString())
        }

        return emptyList()
    }

    private fun searchWordsBySenses(query: String, limit: Int): List<WordMatch> {

        val sanitizedQuery = query.trim()

        if (sanitizedQuery.isNotEmpty()) {
            return queryWordMatches(SEARCH_SENSES_SQL, sanitizedQuery, limit.toString())
        }

        return emptyList()
    }

    private fun searchWordsByRomaji(query: String, limit: Int): List<WordMatch> {
        val wordMatches = mutableListOf<WordMatch>()

        val hiragana = mojiConverter.convertRomajiToHiragana(query)
        wordMatches.addAll(searchWordsByLiterals(hiragana, limit / 2))

        val katakana = mojiConverter.convertRomajiToKatakana(query)
        wordMatches.addAll(searchWordsByLiterals(katakana, limit / 2))

        wordMatches.sortWith(WordMatch.ScoreComparator())

        return wordMatches
    }

    private fun queryWordMatches(sql: String, vararg parameters: String): List<WordMatch> {

        val wordMatches = mutableListOf<WordMatch>()

        executeQuery(sql, *parameters).use {

            while (it.moveToNext()) {

                wordMatches.add(WordMatch(
                        it.getLong(0),
                        it.getString(1),
                        it.getString(2),
                        it.getFloat(3)
                ))
            }
        }

        return wordMatches
    }

    private fun readStringMap(sql: String): Map<String, String> {

        val map = mutableMapOf<String, String>()

        executeQuery(sql).use {

            while (it.moveToNext()) {
                map.put(it.getString(0), it.getString(1))
            }
        }

        return map
    }

    private fun executeQuery(sql: String, vararg parameters: String) = database.rawQuery(sql, parameters)

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
                "select id, original, translated, highlight(sentences_fts, 0, '{', '}') tokenized from sentences join sentences_fts(?) on (id = sentence_id) order by rank limit ?;"
    }
}
