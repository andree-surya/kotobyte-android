package com.kotobyte.models.db;

import com.kotobyte.models.Kanji;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.regex.Pattern;

class KanjiEntryDecoder {

    private Pattern mStringItemsDelimiter = Pattern.compile(";");
    private Kanji.Builder mKanjiBuilder = new Kanji.Builder();

    Kanji decode(long kanjiId, String jsonString) {

        try {
            JSONArray fields = new JSONArray(jsonString);

            mKanjiBuilder.setId(kanjiId);
            mKanjiBuilder.setCharacter(fields.getString(0));
            mKanjiBuilder.setJlpt((short) fields.getInt(3));
            mKanjiBuilder.setGrade((short) fields.getInt(4));

            String readingsField = fields.getString(1);
            String meaningsField = fields.getString(2);
            String strokesField = fields.getString(5);

            if (! "0".equals(readingsField)) {
                mKanjiBuilder.addReadings(mStringItemsDelimiter.split(readingsField));
            }

            if (! "0".equals(meaningsField)) {
                mKanjiBuilder.addMeanings(mStringItemsDelimiter.split(meaningsField));
            }

            if (! "0".equals(strokesField)) {
                mKanjiBuilder.addStrokes(mStringItemsDelimiter.split(strokesField));
            }

            return mKanjiBuilder.build();

        } catch (JSONException e) {
            throw new RuntimeException(e);

        } finally {
            mKanjiBuilder.reset();
        }
    }
}
