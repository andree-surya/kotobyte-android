package com.kotobyte.utils;

import java.io.File;

public class DictionaryDatabase {

    static {
        System.loadLibrary("dictionary_database");
    }

    public DictionaryDatabase(File dictionaryFilePath) {
    }

    public void rebuildIndexes() {

        try {
            Thread.sleep(3000);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        nativeRebuildIndexes();
    }

    private native void nativeRebuildIndexes();
}
