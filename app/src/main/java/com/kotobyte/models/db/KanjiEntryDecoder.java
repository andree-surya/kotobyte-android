package com.kotobyte.models.db;

import com.kotobyte.models.Kanji;

class KanjiEntryDecoder implements DictionaryEntryDecoder<Kanji> {

    private Kanji.Builder mKanjiBuilder = new Kanji.Builder();

    @Override
    public Kanji decode(String encodedObject) {
        String[] kanjiFieldTokens = SPLITTER_L4.split(encodedObject, -1);

        mKanjiBuilder.setID(Long.decode(kanjiFieldTokens[0]));
        mKanjiBuilder.setLiteral(kanjiFieldTokens[1]);

        parseReadings(kanjiFieldTokens[2]);
        parseMeanings(kanjiFieldTokens[3]);
        parseStrokes(kanjiFieldTokens[6]);

        return mKanjiBuilder.buildAndReset();
    }

    private void parseReadings(String readingsField) {

        if (! readingsField.isEmpty()) {
            mKanjiBuilder.setReadings(SPLITTER_L1.split(readingsField));
        }
    }

    private void parseMeanings(String meaningsField) {

        if (! meaningsField.isEmpty()) {
            mKanjiBuilder.setMeanings(SPLITTER_L1.split(meaningsField));
        }
    }

    private void parseStrokes(String strokesField) {

        if (! strokesField.isEmpty()) {
            mKanjiBuilder.setStrokes(SPLITTER_L1.split(strokesField));
        }
    }
}
