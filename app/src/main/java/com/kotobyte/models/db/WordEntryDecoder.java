package com.kotobyte.models.db;

import com.kotobyte.models.Literal;
import com.kotobyte.models.Origin;
import com.kotobyte.models.Sense;
import com.kotobyte.models.Word;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

class WordEntryDecoder {

    private Pattern mStringItemsDelimiter = Pattern.compile(";");
    private Pattern mHighlightsMarker = Pattern.compile("[\\{\\}]");

    private Word.Builder mWordBuilder = new Word.Builder();
    private Sense.Builder mSenseBuilder = new Sense.Builder();

    Word decode(long wordId, String jsonString, String highlights) {

        Map<String, String> highlightsMap = createHighlightsMap(highlights);

        try {
            JSONArray fields = new JSONArray(jsonString);

            mWordBuilder.setId(wordId);

            Literal[] literals = parseLiteralsField(fields.getString(0), highlightsMap);
            Literal[] readings = parseLiteralsField(fields.getString(1), highlightsMap);
            Sense[] senses = parseSensesField(fields.getString(2), highlightsMap);

            if (literals != null) {
                mWordBuilder.addLiterals(literals);
            }

            if (readings != null) {
                mWordBuilder.addReadings(readings);
            }

            if (senses != null) {
                mWordBuilder.addSenses(senses);
            }

            return mWordBuilder.build();

        } catch (JSONException e) {
            throw new RuntimeException(e);

        } finally {
            mWordBuilder.reset();
        }
    }

    private Literal[] parseLiteralsField(String literalsField, Map<String, String> highlightsMap) throws JSONException {

        if (! isEmptyField(literalsField)) {

            JSONArray rawLiterals = new JSONArray(literalsField);
            Literal[] literals = new Literal[rawLiterals.length()];

            for (int i = 0; i < rawLiterals.length(); i++) {
                String rawLiteral = rawLiterals.getString(i);

                String text = rawLiteral.substring(1);
                Literal.Priority priority = null;

                switch (rawLiteral.charAt(0)) {
                    case '0':
                        priority = Literal.Priority.LOW;
                        break;

                    case '2':
                        priority = Literal.Priority.HIGH;
                        break;

                    default:
                        priority = Literal.Priority.NORMAL;
                        break;
                }

                // Replace with highlighted text if needed.
                if (highlightsMap.containsKey(text)) {
                    text = highlightsMap.get(text);
                }

                literals[i] = new Literal(text, priority);
            }

            return literals;
        }

        return null;
    }

    private Sense[] parseSensesField(String sensesField, Map<String, String> highlightsMap) throws JSONException {

        if (! isEmptyField(sensesField)) {

            try {
                JSONArray rawSenses = new JSONArray(sensesField);
                Sense[] senses = new Sense[rawSenses.length()];

                for (int i = 0; i < rawSenses.length(); i++) {
                    JSONArray rawSense = rawSenses.getJSONArray(i);

                    String text = rawSense.getString(0);
                    String[] categories = parseRawStringsField(rawSense.getString(1));
                    Origin[] origins = parseOriginsField(rawSense.getString(2));
                    String[] labels = parseRawStringsField(rawSense.getString(3));
                    String[] notes = parseRawStringsField(rawSense.getString(4));

                    // Replace with highlighted text if needed.
                    if (highlightsMap.containsKey(text)) {
                        text = highlightsMap.get(text);
                    }

                    mSenseBuilder.setText(text);

                    if (categories != null) {
                        mSenseBuilder.addCategories(categories);
                    }

                    if (origins != null) {
                        mSenseBuilder.addOrigins(origins);
                    }

                    if (labels != null) {
                        mSenseBuilder.addLabels(labels);
                    }

                    if (notes != null) {
                        mSenseBuilder.addNotes(notes);
                    }

                    senses[i] = mSenseBuilder.build();

                    mSenseBuilder.reset();
                }

                return senses;

            } finally {
                mSenseBuilder.reset();
            }
        }

        return null;
    }

    private String[] parseRawStringsField(String field) {

        if (! isEmptyField(field)) {
            return mStringItemsDelimiter.split(field);
        }

        return null;
    }

    private Origin[] parseOriginsField(String originsField) {

        if (! isEmptyField(originsField)) {
            String[] rawOrigins = mStringItemsDelimiter.split(originsField);
            Origin[] origins = new Origin[rawOrigins.length];

            for (int i = 0; i < origins.length; i++) {

                String rawOrigin = rawOrigins[i];
                int separatorIndex = rawOrigin.indexOf(':');

                if (separatorIndex < 0) {
                    origins[i] = new Origin(rawOrigin, null);

                } else {
                    String languagecode = rawOrigin.substring(0, separatorIndex);
                    String text = rawOrigin.substring(separatorIndex + 1);

                    origins[i] = new Origin(languagecode, text);
                }
            }

            return origins;
        }

        return null;
    }

    private Map<String, String> createHighlightsMap(String highlights) {

        Map<String, String> highlightsMap = new HashMap<>();

        for (String highlightedText : mStringItemsDelimiter.split(highlights)) {
            String plainText = mHighlightsMarker.matcher(highlightedText).replaceAll("");

            highlightsMap.put(plainText, highlightedText);
        }

        return highlightsMap;
    }

    private static boolean isEmptyField(String field) {
        return field == null || field.isEmpty() || "0".equals(field);
    }
}
