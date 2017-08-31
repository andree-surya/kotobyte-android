package com.kotobyte.models.db

import com.kotobyte.models.Literal
import com.kotobyte.models.Origin
import com.kotobyte.models.Sense
import com.kotobyte.models.Word
import org.json.JSONArray


internal class WordEntryDecoder(
        private val labelsMap: Map<String, String> = mapOf(),
        private val languagesMap: Map<String, String> = mapOf()
) {

    fun decode(ID: Long, JSON: String, highlights: String): Word {

        val highlightsMap = createHighlightsMap(highlights)
        val fields = JSONArray(JSON)

        val literals = parseLiteralsField(fields.get(0).toString(), highlightsMap) ?: emptyList()
        val readings = parseLiteralsField(fields.get(1).toString(), highlightsMap) ?: emptyList()
        val senses = parseSensesField(fields.get(2).toString(), highlightsMap) ?: emptyList()

        return Word(ID, literals, readings, senses)
    }

    private fun parseLiteralsField(literalsField: String, highlightsMap: Map<String, String>): List<Literal>? {

        return literalsField.takeIf { it.isNotEmptyField() }?.run {

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

            literals
        }
    }

    private fun parseSensesField(sensesField: String, highlightsMap: Map<String, String>): List<Sense>? {

        return sensesField.takeIf { it.isNotEmptyField() }?.run {

            val objects = JSONArray(sensesField)
            val senses = mutableListOf<Sense>()

            for (i in 0 until objects.length()) {

                val rawSense = objects.getJSONArray(i)
                val text = rawSense.get(0).toString()

                val categories = parseLabelsField(rawSense.get(1).toString()) ?: emptyList()
                val origins = parseOriginsField(rawSense.get(2).toString()) ?: emptyList()

                val extras = mutableListOf<String>().apply {
                    parseLabelsField(rawSense.get(3).toString())?.let { extras -> addAll(extras) }
                    parseRawStringsField(rawSense.get(4).toString())?.let { extras -> addAll(extras) }
                }

                senses.add(Sense(highlightsMap[text] ?: text, categories, extras, origins))
            }

            senses
        }
    }

    private fun parseLabelsField(field: String): List<String>? {

        return field.takeIf { it.isNotEmptyField() }?.run {
            field.split(";").map { labelsMap[it] ?: it }
        }
    }

    private fun parseRawStringsField(field: String): List<String>? =
            field.takeIf { it.isNotEmptyField() }?.run { field.split(";") }

    private fun parseOriginsField(field: String): List<Origin>? {

        return field.takeIf { it.isNotEmptyField() }?.run {

            field.split(";").map {

                val rawOriginFields = it.split(":")

                val language = rawOriginFields.first()
                val text = rawOriginFields.getOrNull(1)

                Origin(languagesMap[language] ?: language, text)
            }
        }
    }

    private fun createHighlightsMap(highlights: String): Map<String, String> {

        val highlightsMap = HashMap<String, String>()

        highlights.split(";").forEach { highlightedText ->
            val plainText = highlightedText.replace("{", "").replace("}", "")

            highlightsMap.put(plainText, highlightedText)
        }

        return highlightsMap
    }

    private fun String?.isNotEmptyField(): Boolean =
            this != null && this.isNotEmpty() && this != "0"
}