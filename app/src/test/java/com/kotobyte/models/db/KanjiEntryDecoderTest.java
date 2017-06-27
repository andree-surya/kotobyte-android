package com.kotobyte.models.db;

import com.kotobyte.models.Kanji;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class KanjiEntryDecoderTest {

    private KanjiEntryDecoder mDecoder;

    @Before
    public void prepareDecoder() {
        mDecoder = new KanjiEntryDecoder();
    }

    @Test
    public void decode_shouldDecodeJsonToKanji() throws Exception {

        Kanji kanji = mDecoder.decode(27531,
                "[\"残\",\"ザン;サン;のこ.る\",\"remainder;leftover;balance\",2,4," +
                        "\"M13.56,21.7;M30.3,24.12;M29.64,39.5\"]");

        assertEquals(27531, kanji.getId());
        assertEquals("残", kanji.getCharacter());
        assertEquals(2, kanji.getJlpt());
        assertEquals(4, kanji.getGrade());

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
        assertEquals(0, mDecoder.decode(1, "[\"残\",0,\"remainder\",2,4,\"M13.5,21.72\"]").getReadings().length);
    }

    @Test
    public void decode_shouldHandleEmptyMeanings() throws Exception {
        assertEquals(0, mDecoder.decode(1, "[\"残\",\"ザン\",0,2,4,\"M13.5,21.72\"]").getMeanings().length);
    }

    @Test
    public void decode_shouldHandleEmptyStrokes() throws Exception {
        assertEquals(0, mDecoder.decode(1, "[\"残\",\"ザン\",\"remainder\",2,4,0]").getStrokes().length);
    }
}
