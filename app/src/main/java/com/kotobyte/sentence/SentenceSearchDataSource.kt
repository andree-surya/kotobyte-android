package com.kotobyte.sentence

import com.kotobyte.base.DatabaseProvider
import com.kotobyte.models.Sentence
import com.kotobyte.search.EntrySearchContracts

class SentenceSearchDataSource(
        private val databaseProvider: DatabaseProvider,
        private val queries: List<String>

) : EntrySearchContracts.DataSource<Sentence> {

    override fun searchEntries() =
        databaseProvider.obtainDatabaseConnection().searchSentences(queries)
}