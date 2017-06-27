package com.kotobyte.base;

import com.kotobyte.models.Kanji;
import com.kotobyte.models.Word;

import java.util.List;

public interface DatabaseConnection {

    List<Word> searchWords(String query);
    List<Kanji> searchKanji(String query);
}
