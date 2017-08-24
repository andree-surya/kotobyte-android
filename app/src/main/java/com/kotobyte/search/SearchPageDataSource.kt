package com.kotobyte.search

import com.kotobyte.base.DatabaseProvider
import com.kotobyte.models.Kanji
import com.kotobyte.models.Word
import io.reactivex.Single

internal class SearchPageDataSource(private val databaseProvider: DatabaseProvider) : SearchPageContracts.DataSource {

    override fun searchWords(query: String): Single<List<Word>> =
            Single.fromCallable { databaseProvider.obtainDatabaseConnection().searchWords(query) }

    override fun searchKanji(query: String): Single<List<Kanji>> =
            Single.fromCallable { databaseProvider.obtainDatabaseConnection().searchKanji(query) }
}
