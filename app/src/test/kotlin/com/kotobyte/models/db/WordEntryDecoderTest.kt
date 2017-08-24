package com.kotobyte.models.db

import com.kotobyte.models.Literal

import org.junit.Before
import org.junit.Test

import java.util.HashMap

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

class WordEntryDecoderTest {

    @Test
    fun decode_shouldDecodeJsonToWord() {

        val (ID, literals, readings, senses) = WordEntryDecoder(LABELS_MAP, LANGUAGES_MAP).decode(1197020,
                "[[\"2我が儘\",\"1我儘\"],[\"0わがまま\"]," + "[[\"selfishness, egoism\",\"adj-na;n\",0,\"uk\",0],[\"disobedience\",0,0,0,0]]]", "")

        assertEquals(1197020, ID)

        assertEquals(2, literals.size)
        assertEquals(1, readings.size)
        assertEquals(2, senses.size)

        assertEquals("我が儘", literals[0].text)
        assertEquals(Literal.Priority.HIGH, literals[0].priority)

        assertEquals("我儘", literals[1].text)
        assertEquals(Literal.Priority.NORMAL, literals[1].priority)

        assertEquals("わがまま", readings[0].text)
        assertEquals(Literal.Priority.LOW, readings[0].priority)

        assertEquals("selfishness, egoism", senses[0].text)
        assertEquals("disobedience", senses[1].text)
    }

    @Test
    fun decode_shouldHandleEmptyLiterals() {

        val (_, literals) = WordEntryDecoder(LABELS_MAP, LANGUAGES_MAP).decode(1,
                "[0,[\"3わがまま\"],[[\"egoism\",0,0,0,0]]]", "")

        assertEquals(0, literals.size)
    }

    @Test
    fun decode_shouldDecodeSensesProperly() {

        val (_, _, _, senses) = WordEntryDecoder(LABELS_MAP, LANGUAGES_MAP).decode(1, "[0,0," +
                "[[\"side job\",\"n;vs\",\"ger:Arbeit\",\"uk;rare\",0]," +
                "[\"albite\",0,\"ger;fin\",0,\"note 1;note 2\"]]]", "")

        assertEquals(2, senses.size)

        assertEquals("side job", senses[0].text)
        assertEquals(2, senses[0].categories.size)
        assertEquals("Common noun", senses[0].categories[0])
        assertEquals("Transitive verb", senses[0].categories[1])
        assertEquals(1, senses[0].origins.size)

        assertEquals("German", senses[0].origins[0].language)
        assertEquals("Arbeit", senses[0].origins[0].text)
        assertEquals(2, senses[0].extras.size)
        assertEquals("Outdated", senses[0].extras[0])
        assertEquals("rare", senses[0].extras[1])

        assertEquals("albite", senses[1].text)
        assertEquals(0, senses[1].categories.size)
        assertEquals(2, senses[1].origins.size)
        assertEquals("German", senses[1].origins[0].language)
        assertNull(senses[1].origins[0].text)
        assertEquals("fin", senses[1].origins[1].language)
        assertNull(senses[1].origins[1].text)
        assertEquals(2, senses[1].extras.size)
        assertEquals("note 1", senses[1].extras[0])
        assertEquals("note 2", senses[1].extras[1])
    }

    @Test
    fun decode_shouldAssignHighlightsToLiteralsIfApplicable() {

        val (_, literals, readings) = WordEntryDecoder(LABELS_MAP, LANGUAGES_MAP).decode(1,
                "[[\"2我が儘\",\"1我儘\"],[\"3わがまま\",\"2わがま\"],0]", "{我儘};{わがま}ま")

        assertEquals("我が儘", literals[0].text)
        assertEquals("{我儘}", literals[1].text)
        assertEquals("{わがま}ま", readings[0].text)
        assertEquals("わがま", readings[1].text)
    }

    @Test
    fun decode_shouldAssignHighlightsToSensesIfApplicable() {

        val (_, _, _, senses) = WordEntryDecoder(LABELS_MAP, LANGUAGES_MAP).decode(1,
                "[0,0,[[\"selfishness, egoism\",0,0,0,0],[\"disobedience\",0,0,0,0],[\"wilfulness\",0,0,0,0]]]]",
                "selfishness, {egoism};dis{obedience}")

        assertEquals("selfishness, {egoism}", senses[0].text)
        assertEquals("dis{obedience}", senses[1].text)
        assertEquals("wilfulness", senses[2].text)
    }

    companion object {
        private val LABELS_MAP = HashMap<String, String>()
        private val LANGUAGES_MAP = HashMap<String, String>()

        init {
            LABELS_MAP.put("adj-na", "Adjective な")
            LABELS_MAP.put("vs", "Transitive verb")
            LABELS_MAP.put("n", "Common noun")
            LABELS_MAP.put("uk", "Outdated")

            LANGUAGES_MAP.put("ger", "German")
            LANGUAGES_MAP.put("chi", "Chinese")
        }
    }
}
