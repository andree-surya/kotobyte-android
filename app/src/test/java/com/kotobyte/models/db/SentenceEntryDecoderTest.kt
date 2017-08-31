package com.kotobyte.models.db

import org.junit.Assert
import org.junit.Test


class SentenceEntryDecoderTest {

    @Test
    fun decode_shouldDecodeBasicSentenceData() {

        val (ID, original, translated, tokens) = SentenceEntryDecoder().decode(32432,
                "彼らはおおいに努力したが結局失敗した。", "They worked hard only to fail.",
                "彼ら(かれら) 大いに[おおいに] 努力 為る(する)[した] 結局 失敗 為る(する)[した]")

        Assert.assertEquals(32432, ID)
        Assert.assertEquals("彼らはおおいに努力したが結局失敗した。", original)
        Assert.assertEquals("They worked hard only to fail.", translated)
        Assert.assertEquals(7, tokens.size)
    }

    @Test
    fun decode_shouldParseTokenHighlightsAndLocations() {

        val (_, _, _, tokens) = SentenceEntryDecoder().decode(1,
                "彼らはおおいに努力したが結局失敗した。", "",
                "彼ら(かれら) {大いに}[おおいに] 努力 為る(する)[{した}] 結局({けっきょく}) {失敗} 為る(する)[した]")

        Assert.assertEquals(7, tokens.size)

        Assert.assertEquals("彼ら", tokens[0].lemma)
        Assert.assertEquals("彼ら", tokens[0].text)
        Assert.assertEquals(0, tokens[0].location.start)
        Assert.assertEquals(1, tokens[0].location.endInclusive)
        Assert.assertFalse(tokens[0].highlighted)

        Assert.assertEquals("大いに", tokens[1].lemma)
        Assert.assertEquals("おおいに", tokens[1].text)
        Assert.assertEquals(3, tokens[1].location.start)
        Assert.assertEquals(6, tokens[1].location.endInclusive)
        Assert.assertTrue(tokens[1].highlighted)

        Assert.assertEquals("努力", tokens[2].lemma)
        Assert.assertEquals("努力", tokens[2].text)
        Assert.assertEquals(7, tokens[2].location.start)
        Assert.assertEquals(8, tokens[2].location.endInclusive)
        Assert.assertFalse(tokens[2].highlighted)

        Assert.assertEquals("為る", tokens[3].lemma)
        Assert.assertEquals("した", tokens[3].text)
        Assert.assertEquals(9, tokens[3].location.start)
        Assert.assertEquals(10, tokens[3].location.endInclusive)
        Assert.assertTrue(tokens[3].highlighted)

        Assert.assertEquals("結局", tokens[4].lemma)
        Assert.assertEquals("結局", tokens[4].text)
        Assert.assertEquals(12, tokens[4].location.start)
        Assert.assertEquals(13, tokens[4].location.endInclusive)
        Assert.assertTrue(tokens[4].highlighted)

        Assert.assertEquals("失敗", tokens[5].lemma)
        Assert.assertEquals("失敗", tokens[5].text)
        Assert.assertEquals(14, tokens[5].location.start)
        Assert.assertEquals(15, tokens[5].location.endInclusive)
        Assert.assertTrue(tokens[5].highlighted)

        Assert.assertEquals("為る", tokens[6].lemma)
        Assert.assertEquals("した", tokens[6].text)
        Assert.assertEquals(16, tokens[6].location.start)
        Assert.assertEquals(17, tokens[6].location.endInclusive)
        Assert.assertFalse(tokens[6].highlighted)
    }
}