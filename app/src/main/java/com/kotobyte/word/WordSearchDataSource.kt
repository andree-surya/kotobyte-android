package com.kotobyte.word

import com.kotobyte.base.DatabaseProvider
import com.kotobyte.models.Word
import com.kotobyte.search.EntrySearchContracts

internal class WordSearchDataSource(
        private val databaseProvider: DatabaseProvider,
        private val query: String

) : EntrySearchContracts.DataSource<Word> {

    override fun searchEntries() = databaseProvider.obtainDatabaseConnection().searchWords(query)
}
