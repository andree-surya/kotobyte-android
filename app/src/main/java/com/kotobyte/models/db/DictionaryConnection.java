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
import java.util.List;

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

            Cursor cursor = mDatabase.rawQuery(SEARCH_KANJI_SQL,
                    new String[] { queryBuilder.toString(), String.valueOf(KANJI_SEARCH_LIMIT) });

            return readKanjiListFromCursor(cursor);
        }

        return Collections.emptyList();
    }

    void buildIndexes() {

        try {
            mDatabase.beginTransaction();

            for (String statementSQL : BUILD_INDEXES_SQL.split(";")) {

                if (! statementSQL.isEmpty()) {
                    mDatabase.execSQL(statementSQL);
                }
            }

            mDatabase.setTransactionSuccessful();

        } finally {
            mDatabase.endTransaction();
        }
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

    private static List<Kanji> readKanjiListFromCursor(Cursor cursor) {

        List<Kanji> kanjiList = new ArrayList<>(cursor.getCount());
        KanjiEntryDecoder kanjiEntryDecoder = new KanjiEntryDecoder();

        while (cursor.moveToNext()) {

            Kanji kanji = kanjiEntryDecoder.decode(
                    cursor.getLong(0),
                    cursor.getString(1));

            kanjiList.add(kanji);
        }

        cursor.close();

        return kanjiList;
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

    private static List<Word> convertWordMatchesToWords(List<WordMatch> wordMatches) {

        List<Word> words = new ArrayList<>(wordMatches.size());
        WordEntryDecoder wordEntryDecoder = new WordEntryDecoder();

        for (WordMatch wordMatch : wordMatches) {

            Word word = wordEntryDecoder.decode(
                    wordMatch.getId(),
                    wordMatch.getJson(),
                    wordMatch.getHighlights());

            words.add(word);
        }

        return words;
    }

    private static final String BUILD_INDEXES_SQL = "" +
            "create virtual table literals_fts using fts5(text, word_id unindexed, priority unindexed, prefix='1 2 3 4 5');\n" +
            "create virtual table senses_fts using fts5(text, word_id unindexed, tokenize='porter');\n" +
            "create virtual table kanji_fts using fts5(text, kanji_id unindexed);\n" +
            "insert into literals_fts select substr(value, 2), words.id, substr(value, 1, 1) from words, json_each(words.json, '$[0]') where type = 'text';\n" +
            "insert into literals_fts select substr(value, 2), words.id, substr(value, 1, 1) from words, json_each(words.json, '$[1]') where type = 'text';\n" +
            "insert into senses_fts select json_extract(value, '$[0]'), words.id from words, json_each(words.json, '$[2]');\n" +
            "insert into kanji_fts select json_extract(json, '$[0]'), kanji.id from kanji;";

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
}