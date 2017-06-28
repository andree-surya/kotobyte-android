package com.kotobyte.models.db;

import com.kotobyte.models.Kanji;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

class KanjiEntryDecoder {

    private Pattern mStringItemsDelimiter = Pattern.compile(";");
    private Kanji.Builder mKanjiBuilder = new Kanji.Builder();

    private Map<String, String> mJlptMap;
    private Map<String, String> mGradesMap;

    KanjiEntryDecoder() {
        this(new HashMap<String, String>(0), new HashMap<String, String>(0));
    }

    KanjiEntryDecoder(Map<String, String> jlptMap, Map<String, String> gradesMap) {

        mJlptMap = jlptMap;
        mGradesMap = gradesMap;
    }

    Kanji decode(long kanjiId, String jsonString) {

        try {
            JSONArray fields = new JSONArray(jsonString);

            mKanjiBuilder.setId(kanjiId);
            mKanjiBuilder.setCharacter(fields.getString(0));

            String readingsField = fields.getString(1);
            String meaningsField = fields.getString(2);
            String jlpt = mJlptMap.get(fields.getString(3));
            String grade = mGradesMap.get(fields.getString(4));
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

            if (jlpt != null) {
                mKanjiBuilder.addExtra(jlpt);
            }

            if (grade != null) {
                mKanjiBuilder.addExtra(grade);
            }

            return mKanjiBuilder.build();

        } catch (JSONException e) {
            throw new RuntimeException(e);

        } finally {
            mKanjiBuilder.reset();
        }
    }
}
