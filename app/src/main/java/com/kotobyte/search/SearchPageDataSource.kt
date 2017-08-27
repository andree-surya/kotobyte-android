package com.kotobyte.search

import com.kotobyte.base.DatabaseProvider

internal class SearchPageDataSource(private val databaseProvider: DatabaseProvider) : SearchPageContracts.DataSource {

    override fun searchWords(query: String) = databaseProvider.obtainDatabaseConnection().searchWords(query)
    override fun searchKanji(query: String) = databaseProvider.obtainDatabaseConnection().searchKanji(query)
}
