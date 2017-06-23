package com.kotobyte.models.db;

import com.kotobyte.models.Literal;
import com.kotobyte.models.Sense;
import com.kotobyte.models.Word;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class WordEntryDecoderTest {

    private WordEntryDecoder mDecoder;

    @Before
    public void prepareDecoder() {
        mDecoder = new WordEntryDecoder();
    }

    @Test
    public void decode_shouldDecodeWordID() {
        Word word = mDecoder.decode("170845__");

        assertEquals(170845, word.getID());
    }

    @Test
    public void decode_shouldDecodeWordLiteralsAndReadings() {
        Word word = mDecoder.decode("0_3食べカス]2食べかす]1食べ粕]2たべかす]1タベカス_");
        Literal[] literals = word.getLiterals();
        Literal[] readings = word.getReadings();

        assertEquals(3, literals.length);
        assertEquals(2, readings.length);

        assertEquals("食べカス", literals[0].getText());
        assertEquals("食べかす", literals[1].getText());
        assertEquals("食べ粕", literals[2].getText());

        assertEquals("たべかす", readings[0].getText());
        assertEquals("タベカス", readings[1].getText());

        assertEquals(Literal.Status.COMMON, literals[0].getStatus());
        assertEquals(Literal.Status.IRREGULAR, literals[2].getStatus());
        assertEquals(Literal.Status.IRREGULAR, readings[1].getStatus());

        assertNull(literals[1].getStatus());
        assertNull(readings[0].getStatus());
    }

    @Test
    public void decode_shouldDecodeWordSenses() {
        String string = "0__}}}}>}}}}>}}}}";
        Sense[] senses = mDecoder.decode(string).getSenses();

        assertEquals(3, senses.length);

        assertEquals(0, senses[0].getTexts().length);
        assertEquals(0, senses[0].getCategories().length);
    }

    @Test
    public void decode_shouldDecodeSenseTexts() {
        String string = "0__to be able to eat}}}}>to be edible]to be good to eat}}}}";
        Sense[] senses = mDecoder.decode(string).getSenses();

        assertEquals(1, senses[0].getTexts().length);
        assertEquals(2, senses[1].getTexts().length);

        assertEquals("to be able to eat", senses[0].getTexts()[0]);
        assertEquals("to be edible", senses[1].getTexts()[0]);
        assertEquals("to be good to eat", senses[1].getTexts()[1]);
    }

    @Test
    public void decode_shouldDecodeSenseCategories() {
        String string = "0__}n]adj-n}}}>}}}}>}vi}}}";
        Sense[] senses = mDecoder.decode(string).getSenses();

        assertEquals(2, senses[0].getCategories().length);
        assertEquals(2, senses[1].getCategories().length);
        assertEquals(1, senses[2].getCategories().length);

        assertEquals("n", senses[0].getCategories()[0]);
        assertEquals("adj-n", senses[0].getCategories()[1]);
        assertEquals("n", senses[1].getCategories()[0]);
        assertEquals("adj-n", senses[1].getCategories()[1]);
        assertEquals("vi", senses[2].getCategories()[0]);
    }

    @Test
    public void decode_shouldDecodeWordOrigins() {
        String string = "0__}}por:espada]kor]ger}}>}}fre]chi:bēngzi}}>}}eng:ice}}";
        Sense[] senses = mDecoder.decode(string).getSenses();

        assertEquals(3, senses[0].getOrigins().length);
        assertEquals(2, senses[1].getOrigins().length);
        assertEquals(1, senses[2].getOrigins().length);

        assertEquals("por", senses[0].getOrigins()[0].getLanguageCode());
        assertEquals("kor", senses[0].getOrigins()[1].getLanguageCode());
        assertEquals("ger", senses[0].getOrigins()[2].getLanguageCode());
        assertEquals("fre", senses[1].getOrigins()[0].getLanguageCode());
        assertEquals("chi", senses[1].getOrigins()[1].getLanguageCode());
        assertEquals("eng", senses[2].getOrigins()[0].getLanguageCode());

        assertEquals("espada", senses[0].getOrigins()[0].getText());
        assertEquals("bēngzi", senses[1].getOrigins()[1].getText());
        assertEquals("ice", senses[2].getOrigins()[0].getText());

        assertNull(senses[0].getOrigins()[1].getText());
        assertNull(senses[0].getOrigins()[2].getText());
        assertNull(senses[1].getOrigins()[0].getText());
    }

    @Test
    public void decode_shouldDecodeSenseLabels() {
        String string = "0__}}}food]abbr}>}}}comp}>}}}}";
        Sense[] senses = mDecoder.decode(string).getSenses();

        assertEquals(2, senses[0].getLabels().length);
        assertEquals(1, senses[1].getLabels().length);
        assertEquals(0, senses[2].getLabels().length);

        assertEquals("food", senses[0].getLabels()[0]);
        assertEquals("abbr", senses[0].getLabels()[1]);
        assertEquals("comp", senses[1].getLabels()[0]);
    }

    @Test
    public void decode_shouldDecodeSenseNotes() {
        String string = "0__}}}}backpacker slang]esp. 甚い>}}}}>}}}}esp. human-induced";
        Sense[] senses = mDecoder.decode(string).getSenses();

        assertEquals(2, senses[0].getNotes().length);
        assertEquals(0, senses[1].getNotes().length);
        assertEquals(1, senses[2].getNotes().length);

        assertEquals("backpacker slang", senses[0].getNotes()[0]);
        assertEquals("esp. 甚い", senses[0].getNotes()[1]);
        assertEquals("esp. human-induced", senses[2].getNotes()[0]);
    }
}
