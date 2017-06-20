package com.kotobyte.models.db;

import com.kotobyte.models.WordLiteral;
import com.kotobyte.models.WordSense;
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
        Word word = mDecoder.decode("170845‡‡‡");

        assertEquals(170845, word.getID());
    }

    @Test
    public void decode_shouldDecodeWordLiterals() {
        WordLiteral[] wordLiterals = mDecoder.decode("0‡+食べカス⋮=食べかす⋮-食べ粕⋮-食べ滓‡‡").getLiterals();

        assertEquals(4, wordLiterals.length);

        assertEquals("食べカス", wordLiterals[0].getText());
        assertEquals("食べかす", wordLiterals[1].getText());
        assertEquals("食べ粕", wordLiterals[2].getText());
        assertEquals("食べ滓", wordLiterals[3].getText());

        assertEquals(WordLiteral.Status.COMMON, wordLiterals[0].getStatus());
        assertNull(wordLiterals[1].getStatus());
        assertEquals(WordLiteral.Status.IRREGULAR, wordLiterals[2].getStatus());
        assertEquals(WordLiteral.Status.IRREGULAR, wordLiterals[3].getStatus());
    }

    @Test
    public void decode_shouldDecodeWordReadings() {
        WordLiteral[] readings = mDecoder.decode("0‡‡+ガンガンたべる⋮-がんがんたべる‡").getReadings();

        assertEquals(2, readings.length);

        assertEquals("ガンガンたべる", readings[0].getText());
        assertEquals("がんがんたべる", readings[1].getText());

        assertEquals(WordLiteral.Status.COMMON, readings[0].getStatus());
        assertEquals(WordLiteral.Status.IRREGULAR, readings[1].getStatus());
    }

    @Test
    public void decode_shouldDecodeWordSenses() {
        String string = "0‡‡‡¦¦¦¦†¦¦¦¦†¦¦¦¦";
        WordSense[] wordSenses = mDecoder.decode(string).getSenses();

        assertEquals(3, wordSenses.length);

        assertEquals(0, wordSenses[0].getTexts().length);
        assertEquals(0, wordSenses[0].getCategories().length);
    }

    @Test
    public void decode_shouldDecodeSenseTexts() {
        String string = "0‡‡‡to be able to eat¦¦¦¦†to be edible⋮to be good to eat¦¦¦¦";
        WordSense[] wordSenses = mDecoder.decode(string).getSenses();

        assertEquals(1, wordSenses[0].getTexts().length);
        assertEquals(2, wordSenses[1].getTexts().length);

        assertEquals("to be able to eat", wordSenses[0].getTexts()[0]);
        assertEquals("to be edible", wordSenses[1].getTexts()[0]);
        assertEquals("to be good to eat", wordSenses[1].getTexts()[1]);
    }

    @Test
    public void decode_shouldDecodeSenseCategories() {
        String string = "0‡‡‡¦n⋮adj-n¦¦¦†¦¦¦¦†¦vi¦¦¦";
        WordSense[] wordSenses = mDecoder.decode(string).getSenses();

        assertEquals(2, wordSenses[0].getCategories().length);
        assertEquals(2, wordSenses[1].getCategories().length);
        assertEquals(1, wordSenses[2].getCategories().length);

        assertEquals("n", wordSenses[0].getCategories()[0]);
        assertEquals("adj-n", wordSenses[0].getCategories()[1]);
        assertEquals("n", wordSenses[1].getCategories()[0]);
        assertEquals("adj-n", wordSenses[1].getCategories()[1]);
        assertEquals("vi", wordSenses[2].getCategories()[0]);
    }

    @Test
    public void decode_shouldDecodeWordOrigins() {
        String string = "0‡‡‡¦¦por:espada⋮kor⋮ger¦¦†¦¦fre⋮chi:bēngzi¦¦†¦¦eng:ice¦¦";
        WordSense[] wordSenses = mDecoder.decode(string).getSenses();

        assertEquals(3, wordSenses[0].getOrigins().length);
        assertEquals(2, wordSenses[1].getOrigins().length);
        assertEquals(1, wordSenses[2].getOrigins().length);

        assertEquals("por", wordSenses[0].getOrigins()[0].getLanguageCode());
        assertEquals("kor", wordSenses[0].getOrigins()[1].getLanguageCode());
        assertEquals("ger", wordSenses[0].getOrigins()[2].getLanguageCode());
        assertEquals("fre", wordSenses[1].getOrigins()[0].getLanguageCode());
        assertEquals("chi", wordSenses[1].getOrigins()[1].getLanguageCode());
        assertEquals("eng", wordSenses[2].getOrigins()[0].getLanguageCode());

        assertEquals("espada", wordSenses[0].getOrigins()[0].getText());
        assertEquals("bēngzi", wordSenses[1].getOrigins()[1].getText());
        assertEquals("ice", wordSenses[2].getOrigins()[0].getText());

        assertNull(wordSenses[0].getOrigins()[1].getText());
        assertNull(wordSenses[0].getOrigins()[2].getText());
        assertNull(wordSenses[1].getOrigins()[0].getText());
    }

    @Test
    public void decode_shouldDecodeSenseLabels() {
        String string = "0‡‡‡¦¦¦food⋮abbr¦†¦¦¦comp¦†¦¦¦¦";
        WordSense[] wordSenses = mDecoder.decode(string).getSenses();

        assertEquals(2, wordSenses[0].getLabels().length);
        assertEquals(1, wordSenses[1].getLabels().length);
        assertEquals(0, wordSenses[2].getLabels().length);

        assertEquals("food", wordSenses[0].getLabels()[0]);
        assertEquals("abbr", wordSenses[0].getLabels()[1]);
        assertEquals("comp", wordSenses[1].getLabels()[0]);
    }

    @Test
    public void decode_shouldDecodeSenseNotes() {
        String string = "0‡‡‡¦¦¦¦backpacker slang⋮esp. 甚い†¦¦¦¦†¦¦¦¦esp. human-induced";
        WordSense[] wordSenses = mDecoder.decode(string).getSenses();

        assertEquals(2, wordSenses[0].getNotes().length);
        assertEquals(0, wordSenses[1].getNotes().length);
        assertEquals(1, wordSenses[2].getNotes().length);

        assertEquals("backpacker slang", wordSenses[0].getNotes()[0]);
        assertEquals("esp. 甚い", wordSenses[0].getNotes()[1]);
        assertEquals("esp. human-induced", wordSenses[2].getNotes()[0]);
    }
}
