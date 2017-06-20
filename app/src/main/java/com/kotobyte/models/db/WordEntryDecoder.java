package com.kotobyte.models.db;

import com.kotobyte.models.WordLiteral;
import com.kotobyte.models.WordOrigin;
import com.kotobyte.models.WordSense;
import com.kotobyte.models.Word;

import java.util.regex.Pattern;

class WordEntryDecoder implements DictionaryEntryDecoder<Word> {

    private static Pattern WORD_FIELDS_SPLITTER = Pattern.compile("‡");
    private static Pattern SENSE_ITEMS_SPLITTER = Pattern.compile("†");
    private static Pattern SENSE_FIELDS_SPLITTER = Pattern.compile("¦");
    private static Pattern STRING_ITEMS_SPLITTER = Pattern.compile("⋮");
    private static Pattern ORIGIN_FIELDS_SPLITTER = Pattern.compile(":");

    private Word.Builder mWordBuilder = new Word.Builder();
    private WordSense.Builder mSenseBuilder = new WordSense.Builder();

    @Override
    public Word decode(String encodedObject) {
        String[] wordFieldTokens = WORD_FIELDS_SPLITTER.split(encodedObject, -1);

        mWordBuilder.setID(Long.decode(wordFieldTokens[0]));

        parseLiterals(wordFieldTokens[1]);
        parseReadings(wordFieldTokens[2]);
        parseSenses(wordFieldTokens[3]);

        return mWordBuilder.buildAndReset();
    }

    private void parseLiterals(String literalsField) {

        if (literalsField.isEmpty()) {
            return;
        }

        for (String literalToken : STRING_ITEMS_SPLITTER.split(literalsField)) {
            mWordBuilder.addLiteral(decodeLiteral(literalToken));
        }
    }

    private void parseReadings(String readingsField) {

        if (readingsField.isEmpty()) {
            return;
        }

        for (String readingToken : STRING_ITEMS_SPLITTER.split(readingsField)) {
            mWordBuilder.addReading(decodeLiteral(readingToken));
        }
    }

    private void parseSenses(String sensesField) {

        if (sensesField.isEmpty()) {
            return;
        }

        String[] mLastSenseCategories = null;

        for (String senseToken : SENSE_ITEMS_SPLITTER.split(sensesField)) {
            String[] senseFieldTokens = SENSE_FIELDS_SPLITTER.split(senseToken, -1);

            parseSenseTexts(senseFieldTokens[0]);

            mLastSenseCategories = parseSenseCategories(senseFieldTokens[1], mLastSenseCategories);

            parseWordOrigins(senseFieldTokens[2]);
            parseSenseLabels(senseFieldTokens[3]);
            parseSenseNotes(senseFieldTokens[4]);

            mWordBuilder.addSense(mSenseBuilder.buildAndReset());
        }
    }

    private void parseSenseTexts(String senseTextsField) {

        if (senseTextsField.isEmpty()) {
            return;
        }

        mSenseBuilder.addTexts(STRING_ITEMS_SPLITTER.split(senseTextsField));
    }

    private String[] parseSenseCategories(String senseCategoriesField, String[] lastSenseCategories) {

        if (! senseCategoriesField.isEmpty()) {
            lastSenseCategories = STRING_ITEMS_SPLITTER.split(senseCategoriesField);
        }

        if (lastSenseCategories != null) {
            mSenseBuilder.addCategories(lastSenseCategories);
        }

        return lastSenseCategories;
    }

    private void parseWordOrigins(String wordOriginsField) {

        if (wordOriginsField.isEmpty()) {
            return;
        }

        for (String originToken : STRING_ITEMS_SPLITTER.split(wordOriginsField)) {
            mSenseBuilder.addOrigin(decodeOrigin(originToken));
        }
    }

    private void parseSenseLabels(String senseLabelsField) {

        if (senseLabelsField.isEmpty()) {
            return;
        }

        mSenseBuilder.addLabels(STRING_ITEMS_SPLITTER.split(senseLabelsField));
    }

    private void parseSenseNotes(String senseNotesField) {

        if (senseNotesField.isEmpty()) {
            return;
        }

        mSenseBuilder.addNotes(STRING_ITEMS_SPLITTER.split(senseNotesField));
    }

    private static WordLiteral decodeLiteral(String encodedLiteral) {
        String text = encodedLiteral.substring(1, encodedLiteral.length());

        WordLiteral.Status status = null;
        char statusCode = encodedLiteral.charAt(0);

        if (statusCode == '+') {
            status = WordLiteral.Status.COMMON;
        }

        if (statusCode == '-') {
            status = WordLiteral.Status.IRREGULAR;
        }

        return new WordLiteral(text, status);
    }

    private static WordOrigin decodeOrigin(String encodedOrigin) {
        String[] originFields = ORIGIN_FIELDS_SPLITTER.split(encodedOrigin);

        String languageCode = originFields[0];
        String text = originFields.length > 1 ? originFields[1] : null;

        return new WordOrigin(languageCode, text);
    }
}
