package com.kotobyte.base;

import com.kotobyte.models.Kanji;
import com.kotobyte.models.Word;

import java.util.List;

import io.reactivex.Single;

public interface DataRepository {

    Single<List<Word>> searchWords(String query);
    Single<List<Kanji>> searchKanji(String query);
}
