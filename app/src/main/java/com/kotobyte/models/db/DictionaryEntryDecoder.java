package com.kotobyte.models.db;

import java.util.regex.Pattern;

interface DictionaryEntryDecoder<T> {

    Pattern SPLITTER_L1 = Pattern.compile("⋮");
    Pattern SPLITTER_L2 = Pattern.compile("¦");
    Pattern SPLITTER_L3 = Pattern.compile("†");
    Pattern SPLITTER_L4 = Pattern.compile("‡");

    T decode(String encodedObject);
}
