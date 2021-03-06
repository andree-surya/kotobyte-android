package com.kotobyte.kanji

import com.kotobyte.base.DatabaseProvider
import com.kotobyte.models.Kanji
import com.kotobyte.search.EntrySearchContracts

class KanjiSearchDataSource(
        private val databaseProvider: DatabaseProvider,
        private val queries: List<String>

) : EntrySearchContracts.DataSource<Kanji> {

    override fun searchEntries() =
        databaseProvider.obtainDatabaseConnection().searchKanji(queries)
}