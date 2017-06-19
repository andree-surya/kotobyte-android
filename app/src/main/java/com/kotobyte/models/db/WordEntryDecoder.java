package com.kotobyte.models.db;

import com.kotobyte.models.Literal;
import com.kotobyte.models.Sense;
import com.kotobyte.models.Word;

class WordEntryDecoder implements DictionaryEntryDecoder<Word> {

    private Word.Builder mWordBulder = new Word.Builder();
    private Sense.Builder mSenseBuilder = new Sense.Builder();

    @Override
    public Word decode(String encodedObject) {
        String[] wordFieldTokens = SPLITTER_L4.split(encodedObject, -1);

        mWordBulder.setID(Long.decode(wordFieldTokens[0]));

        parseLiterals(wordFieldTokens[1]);
        parseReadings(wordFieldTokens[2]);
        parseSenses(wordFieldTokens[3]);

        return mWordBulder.buildAndReset();
    }

    private void parseLiterals(String literalsField) {

        if (! literalsField.isEmpty()) {

            for (String literalToken : SPLITTER_L3.split(literalsField)) {
                mWordBulder.addLiteral(decodeLiteral(literalToken));
            }
        }
    }

    private void parseReadings(String readingsField) {

        for (String readingToken : SPLITTER_L3.split(readingsField)) {
            mWordBulder.addReading(decodeLiteral(readingToken));
        }
    }

    private void parseSenses(String sensesField) {

        String[] mLastSenseCategories = null;

        for (String senseToken : SPLITTER_L3.split(sensesField)) {
            String[] senseFieldTokens = SPLITTER_L2.split(senseToken, -1);

            mSenseBuilder.setTexts(SPLITTER_L1.split(senseFieldTokens[0]));

            if (! senseFieldTokens[1].isEmpty()) {
                mLastSenseCategories = SPLITTER_L1.split(senseFieldTokens[1]);
            }

            if (mLastSenseCategories != null) {
                mSenseBuilder.setCategories(mLastSenseCategories);
            }

            mWordBulder.addSense(mSenseBuilder.buildAndReset());
        }
    }

    private static Literal decodeLiteral(String encodedLiteral) {
        String text = encodedLiteral.substring(0, encodedLiteral.length());

        Literal.Status status = null;
        char statusCode = encodedLiteral.charAt(0);

        if (statusCode == '+') {
            status = Literal.Status.COMMON;
        }

        if (statusCode == '-') {
            status = Literal.Status.IRREGULAR;
        }

        return new Literal(text, status);
    }
}
