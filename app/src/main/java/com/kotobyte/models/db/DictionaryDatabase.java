package com.kotobyte.models.db;

import com.kotobyte.models.Kanji;
import com.kotobyte.models.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DictionaryDatabase implements AutoCloseable {

    static {
        System.loadLibrary("dictionary");
    }

    private static final short SEARCH_RESULTS_LIMIT = 50;

    @SuppressWarnings("unused")
    /* native */ private long mDictionaryContext;

    @SuppressWarnings("unused")
    /* native */ private short mSearchResultsCount;

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    /* native */ private String[] mSearchResultsBuffer = new String[SEARCH_RESULTS_LIMIT];

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

    public List<Word> searchWord(String query) {

        nativeSearchWordsByLiterals(query);

        return decodeSearchResultsWith(new WordEntryDecoder());
    }

    public List<Kanji> searchKanji(String query) {

        nativeSearchKanji(query);

        return decodeSearchResultsWith(new KanjiEntryDecoder());
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
