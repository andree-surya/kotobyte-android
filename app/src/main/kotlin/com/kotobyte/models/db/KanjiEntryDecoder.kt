package com.kotobyte.models.db

import com.kotobyte.models.Kanji
import org.json.JSONArray
import java.util.regex.Pattern

internal class KanjiEntryDecoder(
        private val JLPTMap: Map<String, String> = mapOf(),
        private val gradesMap: Map<String, String> = mapOf()
) {

    private val stringItemsDelimiter = Pattern.compile(";")
    private val kanjiBuilder = Kanji.Builder()

    fun decode(kanjiId: Long, jsonString: String): Kanji {

        try {
            val fields = JSONArray(jsonString)

            kanjiBuilder.setID(kanjiId)
            kanjiBuilder.setCharacter(fields.get(0).toString()[0])

            val readingsField = fields.get(1).toString()
            val meaningsField = fields.get(2).toString()
            val JLPT = JLPTMap[fields.get(3).toString()]
            val grade = gradesMap[fields.get(4).toString()]
            val strokesField = fields.get(5).toString()

            if ("0" != readingsField) {
                kanjiBuilder.addReadings(stringItemsDelimiter.split(readingsField))
            }

            if ("0" != meaningsField) {
                kanjiBuilder.addMeanings(stringItemsDelimiter.split(meaningsField))
            }

            if ("0" != strokesField) {
                kanjiBuilder.addStrokes(stringItemsDelimiter.split(strokesField))
            }

            if (JLPT != null) {
                kanjiBuilder.addExtra(JLPT)
            }

            if (grade != null) {
                kanjiBuilder.addExtra(grade)
            }

            return kanjiBuilder.build()

        } finally {
            kanjiBuilder.reset()
        }
    }
}
