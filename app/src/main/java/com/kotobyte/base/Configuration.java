package com.kotobyte.base;

import java.io.File;

public interface Configuration {

    String getDictionaryFileName();
    File getDictionaryFilePath();

    int getLatestDictionaryVersion();
    int getCurrentDictionaryVersion();

    void setCurrentDictionaryVersion(int version);
}
