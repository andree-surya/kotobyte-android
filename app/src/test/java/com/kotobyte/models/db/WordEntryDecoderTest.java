package com.kotobyte.models.db;

import com.kotobyte.models.Literal;
import com.kotobyte.models.Sense;
import com.kotobyte.models.Word;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class WordEntryDecoderTest {

    private WordEntryDecoder mDecoder;

    @Before
    public void prepareDecoder() {
        mDecoder = new WordEntryDecoder();
    }

    @Test
    public void decode_shouldDecodeJsonToWord() throws Exception {

        Word word = mDecoder.decode(1197020, "[[\"2我が儘\",\"1我儘\"],[\"0わがまま\"]," +
                "[[\"selfishness, egoism\",\"adj-na;n\",0,\"uk\",0],[\"disobedience\",0,0,0,0]]]", "");

        assertEquals(1197020, word.getId());

        assertEquals(2, word.getLiterals().length);
        assertEquals(1, word.getReadings().length);
        assertEquals(2, word.getSenses().length);

        assertEquals("我が儘", word.getLiterals()[0].getText());
        assertEquals(Literal.Priority.HIGH, word.getLiterals()[0].getPriority());

        assertEquals("我儘", word.getLiterals()[1].getText());
        assertEquals(Literal.Priority.NORMAL, word.getLiterals()[1].getPriority());

        assertEquals("わがまま", word.getReadings()[0].getText());
        assertEquals(Literal.Priority.LOW, word.getReadings()[0].getPriority());

        assertEquals("selfishness, egoism", word.getSenses()[0].getText());
        assertEquals("disobedience", word.getSenses()[1].getText());
    }

    @Test
    public void decode_shouldHandleEmptyLiterals() {
        assertEquals(0, mDecoder.decode(1, "[0,[\"3わがまま\"],[[\"egoism\",0,0,0,0]]]", "").getLiterals().length);
    }

    @Test
    public void decode_shouldDecodeSensesProperly() {

        Word word = mDecoder.decode(1, "[0,0," +
                "[[\"side job\",\"n;vs\",\"ger:Arbeit\",\"uk;rare\",0]," +
                "[\"albite\",0,\"ger;fin\",0,\"note 1;note 2\"]]]", "");

        assertEquals(2, word.getSenses().length);

        Sense sense1 = word.getSenses()[0];
        Sense sense2 = word.getSenses()[1];

        assertEquals("side job", sense1.getText());
        assertEquals(2, sense1.getCategories().length);
        assertEquals("n", sense1.getCategories()[0]);
        assertEquals("vs", sense1.getCategories()[1]);
        assertEquals(1, sense1.getOrigins().length);
        assertEquals("ger", sense1.getOrigins()[0].getLanguageCode());
        assertEquals("Arbeit", sense1.getOrigins()[0].getText());
        assertEquals(2, sense1.getLabels().length);
        assertEquals("uk", sense1.getLabels()[0]);
        assertEquals("rare", sense1.getLabels()[1]);
        assertEquals(0, sense1.getNotes().length);

        assertEquals("albite", sense2.getText());
        assertEquals(0, sense2.getCategories().length);
        assertEquals(2, sense2.getOrigins().length);
        assertEquals("ger", sense2.getOrigins()[0].getLanguageCode());
        assertNull(sense2.getOrigins()[0].getText());
        assertEquals("fin", sense2.getOrigins()[1].getLanguageCode());
        assertNull(sense2.getOrigins()[1].getText());
        assertEquals(0, sense2.getLabels().length);
        assertEquals(2, sense2.getNotes().length);
        assertEquals("note 1", sense2.getNotes()[0]);
        assertEquals("note 2", sense2.getNotes()[1]);
    }

    @Test
    public void decode_shouldAssignHighlightsToLiteralsIfApplicable() {

        Word word = mDecoder.decode(1, "[[\"2我が儘\",\"1我儘\"],[\"3わがまま\",\"2わがま\"],0]", "{我儘};{わがま}ま");

        assertEquals("我が儘", word.getLiterals()[0].getText());
        assertEquals("{我儘}", word.getLiterals()[1].getText());
        assertEquals("{わがま}ま", word.getReadings()[0].getText());
        assertEquals("わがま", word.getReadings()[1].getText());
    }

    @Test
    public void decode_shouldAssignHighlightsToSensesIfApplicable() {

        Word word = mDecoder.decode(1,
                "[0,0,[[\"selfishness, egoism\",0,0,0,0],[\"disobedience\",0,0,0,0],[\"wilfulness\",0,0,0,0]]]]",
                "selfishness, {egoism};dis{obedience}");

        assertEquals("selfishness, {egoism}", word.getSenses()[0].getText());
        assertEquals("dis{obedience}", word.getSenses()[1].getText());
        assertEquals("wilfulness", word.getSenses()[2].getText());
    }
}
