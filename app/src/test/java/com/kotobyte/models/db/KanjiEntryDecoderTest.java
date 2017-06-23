package com.kotobyte.models.db;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class KanjiEntryDecoderTest {

    private KanjiEntryDecoder mDecoder;

    @Before
    public void prepareDecoder() {
        mDecoder = new KanjiEntryDecoder();
    }

    @Test
    public void decode_shouldDecodeKanjiID() {
        assertEquals(314567, mDecoder.decode("314567______").getID());
    }

    @Test
    public void decode_shouldDecodeKanjiLiteral() {
        assertEquals("漢", mDecoder.decode("0_漢_____").getLiteral());
    }

    @Test
    public void decode_shouldDecodeKanjiReadings() {
        String[] readings = mDecoder.decode("0__たべ.る]く.う____").getReadings();

        assertEquals(2, readings.length);
        assertEquals("たべ.る", readings[0]);
        assertEquals("く.う", readings[1]);
    }

    @Test
    public void decode_shouldDecodeKanjiMeanings() {
        String[] meanings = mDecoder.decode("0___to eat]to devour]to consume___").getMeanings();

        assertEquals(3, meanings.length);
        assertEquals("to eat", meanings[0]);
        assertEquals("to devour", meanings[1]);
        assertEquals("to consume", meanings[2]);
    }

    @Test
    public void decode_shouldDecodeKanjiJLPT() {
        assertEquals(4, mDecoder.decode("0____4__").getJLPT());
    }

    @Test
    public void decode_shouldDecodeKanjiGrade() {
        assertEquals(2, mDecoder.decode("0_____2_").getGrade());
    }

    @Test
    public void decode_shouldDecodeKanjiStrokes() {

        String stroke1 = "M15.88,89.23c3.62,0.65,7.25,0.71,10.62,0.49";
        String stroke2 = "M45.99,54.14c0.12,1.15-0.17,2.17-0.68,3.19";
        String stroke3 = "M83.08,21.08c1.17,1.17,1.51,2.92,1.51,4.77";

        String encodedKanji = String.format(Locale.US, "0______%s]%s]%s", stroke1, stroke2, stroke3);
        String[] strokes = mDecoder.decode(encodedKanji).getStrokes();

        assertEquals(3, strokes.length);
        assertEquals(stroke1, strokes[0]);
        assertEquals(stroke2, strokes[1]);
        assertEquals(stroke3, strokes[2]);
    }
}
