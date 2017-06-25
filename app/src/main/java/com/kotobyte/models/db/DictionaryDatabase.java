package com.kotobyte.models.db;

import com.kotobyte.models.Kanji;
import com.kotobyte.models.Word;
import com.moji4j.MojiDetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DictionaryDatabase implements AutoCloseable {

    static {
        System.loadLibrary("dictionary");
    }

    private static final int SEARCH_RESULTS_LIMIT = 50;
    private static final MojiDetector MOJI_DETECTOR = new MojiDetector();

    private final WordEntryDecoder mWordEntryDecoder = new WordEntryDecoder();
    private final KanjiEntryDecoder mKanjiEntryDecoder = new KanjiEntryDecoder();

    @SuppressWarnings("unused")
    /* native */ private long mDictionaryContext;

    @SuppressWarnings("unused")
    /* native */ private int mSearchResultsCount;

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    /* native */ private final String[] mSearchResultsBuffer = new String[SEARCH_RESULTS_LIMIT];

    public DictionaryDatabase(String dictionaryFilePath) {
        this(dictionaryFilePath, true);
    }

    public DictionaryDatabase(String dictionaryFilePath, boolean readOnly) {

        nativeOpenConnection(dictionaryFilePath, readOnly);
    }

    @Override
    public void close() throws Exception {
        nativeCloseConnection();
    }

    public void buildIndexes() {
        nativeBuildIndexes();
    }

    public List<Word> searchWord(String userQuery) {

        if (MOJI_DETECTOR.hasKanji(userQuery) || MOJI_DETECTOR.hasKana(userQuery)) {
            return searchWordByLiterals(userQuery);

        } else if (MOJI_DETECTOR.hasRomaji(userQuery)) {
            return searchWordBySenses(userQuery);

        } else {
            return Collections.emptyList();
        }
    }

    public List<Kanji> searchKanji(String userQuery) {

        if (MOJI_DETECTOR.hasKanji(userQuery)) {
            nativeSearchKanji(userQuery);

            return decodeSearchResultsWith(mKanjiEntryDecoder);
        }

        return Collections.emptyList();
    }

    private List<Word> searchWordByLiterals(String userQuery) {

        nativeSearchWordsByLiterals(userQuery);

        return decodeSearchResultsWith(mWordEntryDecoder);
    }

    private List<Word> searchWordBySenses(String userQuery) {

        nativeSearchWordsBySenses(userQuery);

        return decodeSearchResultsWith(mWordEntryDecoder);
    }

    private <T> List<T> decodeSearchResultsWith(DictionaryEntryDecoder<T> entryDecoder) {

        List<T> entries = new ArrayList<>(mSearchResultsCount);

        for (int i = 0; i < mSearchResultsCount; i++) {
            entries.add(entryDecoder.decode(mSearchResultsBuffer[i]));
        }

        return Collections.unmodifiableList(entries);
    }

    private native void nativeOpenConnection(String filePath, boolean readOnly);
    private native void nativeCloseConnection();
    private native void nativeBuildIndexes();
    private native void nativeSearchWordsByLiterals(String query);
    private native void nativeSearchWordsBySenses(String query);
    private native void nativeSearchKanji(String query);
}
