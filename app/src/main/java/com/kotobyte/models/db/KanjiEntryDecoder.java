package com.kotobyte.models.db;

import com.kotobyte.models.Kanji;

import java.util.regex.Pattern;

class KanjiEntryDecoder implements DictionaryEntryDecoder<Kanji> {

    private static Pattern KANJI_FIELDS_SPLITTER = Pattern.compile("_");
    private static Pattern STRING_ITEMS_SPLITTER = Pattern.compile("]");

    private Kanji.Builder mKanjiBuilder = new Kanji.Builder();

    @Override
    public Kanji decode(String encodedObject) {
        String[] kanjiFieldTokens = KANJI_FIELDS_SPLITTER.split(encodedObject, -1);

        mKanjiBuilder.setID(Long.decode(kanjiFieldTokens[0]));
        mKanjiBuilder.setLiteral(kanjiFieldTokens[1]);

        parseReadings(kanjiFieldTokens[2]);
        parseMeanings(kanjiFieldTokens[3]);
        parseJLPT(kanjiFieldTokens[4]);
        parseGrade(kanjiFieldTokens[5]);
        parseStrokes(kanjiFieldTokens[6]);

        return mKanjiBuilder.buildAndReset();
    }

    private void parseReadings(String readingsField) {

        if (! readingsField.isEmpty()) {
            mKanjiBuilder.addReadings(STRING_ITEMS_SPLITTER.split(readingsField));
        }
    }

    private void parseMeanings(String meaningsField) {

        if (! meaningsField.isEmpty()) {
            mKanjiBuilder.addMeanings(STRING_ITEMS_SPLITTER.split(meaningsField));
        }
    }

    private void parseJLPT(String JLPTField) {

        if (! JLPTField.isEmpty()) {
            mKanjiBuilder.setJLPT(Short.decode(JLPTField));
        }
    }

    private void parseGrade(String gradeField) {

        if (! gradeField.isEmpty()) {
            mKanjiBuilder.setGrade(Short.decode(gradeField));
        }
    }

    private void parseStrokes(String strokesField) {

        if (! strokesField.isEmpty()) {
            mKanjiBuilder.addStrokes(STRING_ITEMS_SPLITTER.split(strokesField));
        }
    }
}
