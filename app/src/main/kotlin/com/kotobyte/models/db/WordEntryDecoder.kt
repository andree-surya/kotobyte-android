package com.kotobyte.models.db

import com.kotobyte.models.Literal
import com.kotobyte.models.Origin
import com.kotobyte.models.Sense
import com.kotobyte.models.Word

import org.json.JSONArray

import java.util.HashMap
import java.util.regex.Pattern

internal class WordEntryDecoder(
        private val labelsMap: Map<String, String> = mapOf(),
        private val languagesMap: Map<String, String> = mapOf()
) {

    private val stringItemsDelimiter = Pattern.compile(";")
    private val highlightsMarker = Pattern.compile("[\\{\\}]")

    private val wordBuilder = Word.Builder()
    private val sensesBuilder = Sense.Builder()

    fun decode(wordId: Long, jsonString: String, highlights: String): Word {

        val highlightsMap = createHighlightsMap(highlights)

        return try {
            val fields = JSONArray(jsonString)

            wordBuilder.setID(wordId)
            wordBuilder.setLiterals(parseLiteralsField(fields.get(0).toString(), highlightsMap))
            wordBuilder.setReadings(parseLiteralsField(fields.get(1).toString(), highlightsMap))
            wordBuilder.setSenses(parseSensesField(fields.get(2).toString(), highlightsMap))

            wordBuilder.build()

        } finally {
            wordBuilder.reset()
        }
    }

    private fun parseLiteralsField(literalsField: String, highlightsMap: Map<String, String>): List<Literal>? {

        if (isNotEmptyField(literalsField)) {

            val rawLiterals = JSONArray(literalsField)
            val literals = mutableListOf<Literal>()

            for (i in 0 until rawLiterals.length()) {
                val rawLiteral = rawLiterals.getString(i)

                val text = rawLiteral.substring(1)

                val priority = when (rawLiteral[0]) {
                    '0' -> Literal.Priority.LOW
                    '2' -> Literal.Priority.HIGH
                    else -> Literal.Priority.NORMAL
                }

                literals.add(Literal(highlightsMap[text] ?: text, priority))
            }

            return literals
        }

        return null
    }

    private fun parseSensesField(sensesField: String, highlightsMap: Map<String, String>): List<Sense>? {

        if (isNotEmptyField(sensesField)) {

            try {
                val rawSenses = JSONArray(sensesField)
                val senses = mutableListOf<Sense>()

                for (i in 0 until rawSenses.length()) {
                    val rawSense = rawSenses.getJSONArray(i)

                    val text = rawSense.get(0).toString()

                    sensesBuilder.setText(highlightsMap[text] ?: text)
                    sensesBuilder.setCategories(parseLabelsField(rawSense.get(1).toString()))
                    sensesBuilder.setOrigins(parseOriginsField(rawSense.get(2).toString()))
                    sensesBuilder.addExtras(parseLabelsField(rawSense.get(3).toString()))
                    sensesBuilder.addExtras(parseRawStringsField(rawSense.get(4).toString()))

                    senses.add(sensesBuilder.build())
                    sensesBuilder.reset()
                }

                return senses

            } finally {
                sensesBuilder.reset()
            }
        }

        return null
    }

    private fun parseLabelsField(field: String): List<String>? {

        if (isNotEmptyField(field)) {
            val labels = stringItemsDelimiter.split(field)

            return labels.map { labelsMap[it] ?: it }.toList()
        }

        return null
    }

    private fun parseRawStringsField(field: String): List<String>? =
            if (isNotEmptyField(field)) stringItemsDelimiter.split(field).toList() else null

    private fun parseOriginsField(originsField: String): List<Origin>? {

        if (isNotEmptyField(originsField)) {

            val rawOrigins = stringItemsDelimiter.split(originsField)
            val origins = mutableListOf<Origin>()

            rawOrigins.forEach { rawOrigin ->

                var language = rawOrigin
                var text: String? = null

                val separatorIndex = rawOrigin.indexOf(':')

                if (separatorIndex > 0) {
                    language = rawOrigin.substring(0, separatorIndex)
                    text = rawOrigin.substring(separatorIndex + 1)
                }

                if (languagesMap.containsKey(language)) {
                    language = languagesMap[language]
                }

                origins.add(Origin(language, text))
            }

            return origins
        }

        return null
    }

    private fun createHighlightsMap(highlights: String): Map<String, String> {

        val highlightsMap = HashMap<String, String>()

        for (highlightedText in stringItemsDelimiter.split(highlights)) {
            val plainText = highlightsMarker.matcher(highlightedText).replaceAll("")

            highlightsMap.put(plainText, highlightedText)
        }

        return highlightsMap
    }

    private fun isNotEmptyField(field: String?): Boolean =
            field != null && field.isNotEmpty() && "0" != field
}
