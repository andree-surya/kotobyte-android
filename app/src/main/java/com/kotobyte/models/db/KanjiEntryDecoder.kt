package com.kotobyte.models.db

import com.kotobyte.models.Kanji
import org.json.JSONArray

internal class KanjiEntryDecoder(
        private val JLPTMap: Map<String, String> = mapOf(),
        private val gradesMap: Map<String, String> = mapOf()
) {

    fun decode(ID: Long, JSON: String): Kanji {

        val fields = JSONArray(JSON)

        val character = fields.get(0).toString().first()
        val readings = fields.get(1).toString().takeUnless { it == "0" }?.split(";") ?: emptyList()
        val meanings = fields.get(2).toString().takeUnless { it == "0" }?.split(";") ?: emptyList()
        val strokes = fields.get(5).toString().takeUnless { it == "0" }?.split(";") ?: emptyList()

        val extras = mutableListOf<String>().apply {

            JLPTMap[fields.get(3).toString()]?.let { extra -> add(extra) }
            gradesMap[fields.get(4).toString()]?.let { extra -> add(extra) }
        }

        return Kanji(ID, character, readings, meanings, strokes, extras)
    }
}
