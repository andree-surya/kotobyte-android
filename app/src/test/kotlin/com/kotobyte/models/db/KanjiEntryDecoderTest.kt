package com.kotobyte.models.db

import org.junit.Assert.assertEquals
import org.junit.Test

class KanjiEntryDecoderTest {

    @Test
    @Throws(Exception::class)
    fun decode_shouldDecodeJsonToKanji() {

        val (ID, character, readings, meanings, strokes) = KanjiEntryDecoder().decode(27531,
                "[\"残\",\"ザン;サン;のこ.る\",\"remainder;leftover;balance\",2,4,\"M13.56,21.7;M30.3,24.12;M29.64,39.5\"]")

        assertEquals(27531, ID)
        assertEquals('残', character)

        assertEquals(3, readings.size)
        assertEquals("ザン", readings[0])
        assertEquals("サン", readings[1])
        assertEquals("のこ.る", readings[2])

        assertEquals(3, meanings.size)
        assertEquals("remainder", meanings[0])
        assertEquals("leftover", meanings[1])
        assertEquals("balance", meanings[2])

        assertEquals(3, strokes.size)
        assertEquals("M13.56,21.7", strokes[0])
        assertEquals("M30.3,24.12", strokes[1])
        assertEquals("M29.64,39.5", strokes[2])
    }

    @Test
    @Throws(Exception::class)
    fun decode_shouldHandleEmptyReadings() {

        val (_, _, readings) = KanjiEntryDecoder().decode(1,
                "[\"残\",0,\"remainder\",2,4,\"M13.5,21.72\"]")

        assertEquals(0, readings.size)
    }

    @Test
    @Throws(Exception::class)
    fun decode_shouldHandleEmptyMeanings() {

        val (_, _, _, meanings) = KanjiEntryDecoder().decode(1,
                "[\"残\",\"ザン\",0,2,4,\"M13.5,21.72\"]")

        assertEquals(0, meanings.size)
    }

    @Test
    @Throws(Exception::class)
    fun decode_shouldHandleEmptyStrokes() {

        val (_, _, _, _, strokes) = KanjiEntryDecoder().decode(1,
                "[\"残\",\"ザン\",\"remainder\",2,4,0]")

        assertEquals(0, strokes.size)
    }

    @Test
    @Throws(Exception::class)
    fun decode_shouldHandleMatchingExtras() {

        val jlptMap = HashMap<String, String>()
        jlptMap.put("1", "JLPT N1")
        jlptMap.put("3", "JLPT N4")

        val gradesMap = HashMap<String, String>()
        gradesMap.put("1", "Elementary school, 1st grade")
        gradesMap.put("7", "High school grade")

        val (_, _, _, _, _, extras) = KanjiEntryDecoder(jlptMap, gradesMap).decode(1, "[\"残\",0,0,3,1,0]")

        assertEquals(2, extras.size)
        assertEquals("JLPT N4", extras[0])
        assertEquals("Elementary school, 1st grade", extras[1])
    }

    @Test
    @Throws(Exception::class)
    fun decode_shouldHandleMissingExtras() {

        val jlptMap = HashMap<String, String>()
        jlptMap.put("2", "JLPT N2/N3")
        jlptMap.put("4", "JLPT N5")

        val gradesMap = HashMap<String, String>()
        gradesMap.put("2", "Elementary school, 2nd grade")
        gradesMap.put("10", "Jinmeiyou (used in name)")

        val (_, _, _, _, _, extras) = KanjiEntryDecoder(jlptMap, gradesMap).decode(1, "[\"残\",0,0,1,3,0]")

        assertEquals(0, extras.size)
    }
}
