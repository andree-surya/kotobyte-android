package com.kotobyte.models.db;

import com.kotobyte.models.Kanji;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class KanjiEntryDecoderTest {

    @Before
    public void prepareDecoder() {

    }

    @Test
    public void decode_shouldDecodeJsonToKanji() throws Exception {

        Kanji kanji = new KanjiEntryDecoder().decode(27531,
                "[\"残\",\"ザン;サン;のこ.る\",\"remainder;leftover;balance\",2,4,\"M13.56,21.7;M30.3,24.12;M29.64,39.5\"]");

        assertEquals(27531, kanji.getId());
        assertEquals("残", kanji.getCharacter());

        assertEquals(3, kanji.getReadings().length);
        assertEquals("ザン", kanji.getReadings()[0]);
        assertEquals("サン", kanji.getReadings()[1]);
        assertEquals("のこ.る", kanji.getReadings()[2]);

        assertEquals(3, kanji.getMeanings().length);
        assertEquals("remainder", kanji.getMeanings()[0]);
        assertEquals("leftover", kanji.getMeanings()[1]);
        assertEquals("balance", kanji.getMeanings()[2]);

        assertEquals(3, kanji.getStrokes().length);
        assertEquals("M13.56,21.7", kanji.getStrokes()[0]);
        assertEquals("M30.3,24.12", kanji.getStrokes()[1]);
        assertEquals("M29.64,39.5", kanji.getStrokes()[2]);
    }

    @Test
    public void decode_shouldHandleEmptyReadings() throws Exception {

        Kanji kanji = new KanjiEntryDecoder().decode(1,
                "[\"残\",0,\"remainder\",2,4,\"M13.5,21.72\"]");

        assertEquals(0, kanji.getReadings().length);
    }

    @Test
    public void decode_shouldHandleEmptyMeanings() throws Exception {

        Kanji kanji = new KanjiEntryDecoder().decode(1,
                "[\"残\",\"ザン\",0,2,4,\"M13.5,21.72\"]");

        assertEquals(0, kanji.getMeanings().length);
    }

    @Test
    public void decode_shouldHandleEmptyStrokes() throws Exception {

        Kanji kanji = new KanjiEntryDecoder().decode(1,
                "[\"残\",\"ザン\",\"remainder\",2,4,0]");

        assertEquals(0, kanji.getStrokes().length);
    }

    @Test
    public void decode_shouldHandleMatchingExtras() throws Exception {

        Map<String, String> jlptMap = new HashMap<>();
        jlptMap.put("1", "JLPT N1");
        jlptMap.put("3", "JLPT N4");

        Map<String, String> gradesMap = new HashMap<>();
        gradesMap.put("1", "Elementary school, 1st grade");
        gradesMap.put("7", "High school grade");

        Kanji kanji = new KanjiEntryDecoder(jlptMap, gradesMap).decode(1, "[\"残\",0,0,3,1,0]");

        assertEquals(2, kanji.getExtras().length);
        assertEquals("JLPT N4", kanji.getExtras()[0]);
        assertEquals("Elementary school, 1st grade", kanji.getExtras()[1]);
    }

    @Test
    public void decode_shouldHandleMissingExtras() throws Exception {

        Map<String, String> jlptMap = new HashMap<>();
        jlptMap.put("2", "JLPT N2/N3");
        jlptMap.put("4", "JLPT N5");

        Map<String, String> gradesMap = new HashMap<>();
        gradesMap.put("2", "Elementary school, 2nd grade");
        gradesMap.put("10", "Jinmeiyou (used in name)");

        Kanji kanji = new KanjiEntryDecoder(jlptMap, gradesMap).decode(1, "[\"残\",0,0,1,3,0]");

        assertEquals(0, kanji.getExtras().length);
    }
}
