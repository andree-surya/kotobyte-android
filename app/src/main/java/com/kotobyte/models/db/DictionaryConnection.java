package com.kotobyte.models.db;

import android.database.Cursor;

import com.kotobyte.base.DatabaseConnection;
import com.kotobyte.models.Kanji;
import com.kotobyte.models.Word;
import com.moji4j.MojiConverter;
import com.moji4j.MojiDetector;

import org.sqlite.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DictionaryConnection implements DatabaseConnection, AutoCloseable {

    static {
        System.loadLibrary("sqliteX");
    }

    private static final int KANJI_SEARCH_LIMIT = 10;
    private static final int WORD_SEARCH_LIMIT = 50;
    private static final int ROMAJI_SEARCH_THRESHOLD = 10;

    private SQLiteDatabase mDatabase;

    private MojiConverter mMojiConverter = new MojiConverter();
    private MojiDetector mMojiDetector = new MojiDetector();

    private Map<String, String> mLabelsMap;
    private Map<String, String> mLanguagesMap;
    private Map<String, String> mJlptMap;
    private Map<String, String> mGradesMap;

    DictionaryConnection(String name, int version) {

        mDatabase = SQLiteDatabase.openDatabase(name, null, SQLiteDatabase.OPEN_READWRITE);
        mDatabase.setVersion(version);
    }

    @Override
    public void close() throws Exception {
        mDatabase.close();
    }

    @Override
    public List<Word> searchWords(String query) {

        List<WordMatch> wordMatches = new ArrayList<>();

        if (mLabelsMap == null) {
            mLabelsMap = readStringMapFromCursor(mDatabase.rawQuery(SELECT_LABELS_SQL, null));
        }

        if (mLanguagesMap == null) {
            mLanguagesMap = readStringMapFromCursor(mDatabase.rawQuery(SELECT_LANGUAGES_SQL, null));
        }

        if (mMojiDetector.hasKanji(query) || mMojiDetector.hasKana(query)) {
            wordMatches.addAll(searchWordsByLiterals(query, WORD_SEARCH_LIMIT));

        } else if (mMojiDetector.hasRomaji(query)) {
            wordMatches.addAll(searchWordsBySenses(query, WORD_SEARCH_LIMIT));

            // Not enough search results in English. User query is probably a romanized Japanese.
            if (wordMatches.size() < ROMAJI_SEARCH_THRESHOLD) {

                int romajiSearchLimit = WORD_SEARCH_LIMIT - wordMatches.size();
                wordMatches.addAll(searchWordsByRomaji(query, romajiSearchLimit));
            }
        }

        return convertWordMatchesToWords(wordMatches);
    }

    @Override
    public List<Kanji> searchKanji(String query) {

        StringBuilder queryBuilder = new StringBuilder();

        for (char character : query.toCharArray()) {

            if (mMojiDetector.isKanji(character)) {

                if (queryBuilder.length() > 0) {
                    queryBuilder.append(" OR ");
                }

                queryBuilder.append(character);
            }
        }

        if (queryBuilder.length() > 0) {

            if (mJlptMap == null) {
                mJlptMap = readStringMapFromCursor(mDatabase.rawQuery(SELECT_JLPT_SQL, null));
            }

            if (mGradesMap == null) {
                mGradesMap = readStringMapFromCursor(mDatabase.rawQuery(SELECT_GRADES_SQL, null));
            }

            Cursor cursor = mDatabase.rawQuery(SEARCH_KANJI_SQL,
                    new String[] { queryBuilder.toString(), String.valueOf(KANJI_SEARCH_LIMIT) });

            return readKanjiListFromCursor(cursor);
        }

        return Collections.emptyList();
    }

    private List<WordMatch> searchWordsByLiterals(String query, int limit) {
        query = query.replaceAll("\\p{Blank}", "");

        StringBuilder queryBuilder = new StringBuilder(query);

        for (int i = query.length(); i > 0; i--) {

            queryBuilder.append(" OR ");
            queryBuilder.append(query.substring(0, i));
            queryBuilder.append('*');
        }

        if (queryBuilder.length() > 0) {

            Cursor cursor = mDatabase.rawQuery(SEARCH_LITERALS_SQL,
                    new String[] { queryBuilder.toString(), String.valueOf(limit) });

            return readWordMatchesFromCursor(cursor);
        }

        return Collections.emptyList();
    }

    private List<WordMatch> searchWordsBySenses(String query, int limit) {
        query = query.trim();

        if (query.length() > 0) {

            Cursor cursor = mDatabase.rawQuery(SEARCH_SENSES_SQL,
                    new String[] { query, String.valueOf(limit) });

            return readWordMatchesFromCursor(cursor);
        }

        return Collections.emptyList();
    }

    private List<WordMatch> searchWordsByRomaji(String query, int limit) {
        List<WordMatch> wordMatches = new ArrayList<>();

        String hiragana = mMojiConverter.convertRomajiToHiragana(query);
        wordMatches.addAll(searchWordsByLiterals(hiragana, limit / 2));

        String katakana = mMojiConverter.convertRomajiToKatakana(query);
        wordMatches.addAll(searchWordsByLiterals(katakana, limit / 2));

        Collections.sort(wordMatches, new WordMatch.ScoreComparator());

        return wordMatches;
    }

    private List<Kanji> readKanjiListFromCursor(Cursor cursor) {

        List<Kanji> kanjiList = new ArrayList<>(cursor.getCount());
        KanjiEntryDecoder kanjiEntryDecoder = new KanjiEntryDecoder(mJlptMap, mGradesMap);

        while (cursor.moveToNext()) {

            Kanji kanji = kanjiEntryDecoder.decode(
                    cursor.getLong(0),
                    cursor.getString(1));

            kanjiList.add(kanji);
        }

        cursor.close();

        return kanjiList;
    }

    private List<Word> convertWordMatchesToWords(List<WordMatch> wordMatches) {

        List<Word> words = new ArrayList<>(wordMatches.size());
        WordEntryDecoder wordEntryDecoder = new WordEntryDecoder(mLabelsMap, mLanguagesMap);

        for (WordMatch wordMatch : wordMatches) {

            Word word = wordEntryDecoder.decode(
                    wordMatch.getId(),
                    wordMatch.getJson(),
                    wordMatch.getHighlights());

            words.add(word);
        }

        return words;
    }

    private static List<WordMatch> readWordMatchesFromCursor(Cursor cursor) {

        List<WordMatch> wordMatches = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {

            WordMatch wordMatch = new WordMatch(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getFloat(3));

            wordMatches.add(wordMatch);
        }

        cursor.close();

        return wordMatches;
    }

    private static Map<String, String> readStringMapFromCursor(Cursor cursor) {

        Map<String, String> map = new HashMap<>();

        while (cursor.moveToNext()) {
            map.put(cursor.getString(0), cursor.getString(1));
        }

        cursor.close();

        return map;
    }

    private static final String SEARCH_WORDS_SQL = "" +
            "with search_results as (%s)\n" +
            "    select id, json, group_concat(highlight, ';') highlights, min(score) score\n" +
            "    from words join search_results on (id = word_id) group by id order by score;";

    private static final String SEARCH_LITERALS_SQL = String.format(SEARCH_WORDS_SQL,
            "select word_id, highlight(literals_fts, 0, '{', '}') highlight, rank * priority score from literals_fts(?) order by score limit ?");

    private static final String SEARCH_SENSES_SQL = String.format(SEARCH_WORDS_SQL,
            "select word_id, highlight(senses_fts, 0, '{', '}') highlight, rank score from senses_fts(?) order by score limit ?");

    private static final String SEARCH_KANJI_SQL =
            "select id, json from kanji join kanji_fts(?) on (id = kanji_id) order by rank limit ?;";

    private static final String SELECT_LABELS_SQL = "select code, text from labels;";
    private static final String SELECT_LANGUAGES_SQL = "select code, text from languages;";
    private static final String SELECT_JLPT_SQL = "select number, text from jlpt;";
    private static final String SELECT_GRADES_SQL = "select number, text from grades;";
}
