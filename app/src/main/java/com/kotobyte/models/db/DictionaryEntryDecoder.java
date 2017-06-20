package com.kotobyte.models.db;

interface DictionaryEntryDecoder<T> {

    T decode(String encodedObject);
}
