package com.kotobyte.kanji

import com.kotobyte.base.DatabaseProvider
import com.kotobyte.models.Kanji
import com.kotobyte.search.EntrySearchContracts

class KanjiSearchDataSource(
        private val databaseProvider: DatabaseProvider,
        queries: List<String>

) : EntrySearchContracts.DataSource<Kanji> {

    private val query = queries.joinToString(" ")

    override fun searchEntries(): List<Kanji> =
        databaseProvider.obtainDatabaseConnection().searchKanji(query)
}