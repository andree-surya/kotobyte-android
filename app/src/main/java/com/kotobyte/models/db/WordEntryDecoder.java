package com.kotobyte.models.db;

import com.kotobyte.models.Literal;
import com.kotobyte.models.Origin;
import com.kotobyte.models.Sense;
import com.kotobyte.models.Word;
import com.moji4j.MojiDetector;

import java.util.regex.Pattern;

class WordEntryDecoder implements DictionaryEntryDecoder<Word> {

    private static Pattern WORD_FIELDS_SPLITTER = Pattern.compile("_");
    private static Pattern SENSE_ITEMS_SPLITTER = Pattern.compile(">");
    private static Pattern SENSE_FIELDS_SPLITTER = Pattern.compile("\\}");
    private static Pattern STRING_ITEMS_SPLITTER = Pattern.compile("\\]");
    private static Pattern ORIGIN_FIELDS_SPLITTER = Pattern.compile(":");

    private Word.Builder mWordBuilder = new Word.Builder();
    private Sense.Builder mSenseBuilder = new Sense.Builder();
    private MojiDetector mMojiDetector = new MojiDetector();

    @Override
    public Word decode(String encodedObject) {
        String[] wordFieldTokens = WORD_FIELDS_SPLITTER.split(encodedObject, -1);

        mWordBuilder.setID(Long.decode(wordFieldTokens[0]));

        parseLiterals(wordFieldTokens[1]);
        parseSenses(wordFieldTokens[2]);

        return mWordBuilder.buildAndReset();
    }

    private void parseLiterals(String literalsField) {

        if (literalsField.isEmpty()) {
            return;
        }

        for (String literalToken : STRING_ITEMS_SPLITTER.split(literalsField)) {

            String text = literalToken.substring(1, literalToken.length());

            Literal.Status status = null;
            char priorityCode = literalToken.charAt(0);

            if (priorityCode == '3') {
                status = Literal.Status.COMMON;
            }

            if (priorityCode == '1') {
                status = Literal.Status.IRREGULAR;
            }

            Literal literal = new Literal(text, status);

            if (mMojiDetector.hasKanji(text)) {
                mWordBuilder.addLiteral(literal);

            } else {
                mWordBuilder.addReading(literal);
            }
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

    private static Origin decodeOrigin(String encodedOrigin) {
        String[] originFields = ORIGIN_FIELDS_SPLITTER.split(encodedOrigin);

        String languageCode = originFields[0];
        String text = originFields.length > 1 ? originFields[1] : null;

        return new Origin(languageCode, text);
    }
}
