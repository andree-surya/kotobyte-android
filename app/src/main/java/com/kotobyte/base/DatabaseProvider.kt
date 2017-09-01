package com.kotobyte.base

interface DatabaseProvider {

    val isMigrationNeeded: Boolean
    val isMigrationPossible: Boolean

    /**
     * Get the shared database connection.
     *
     * Depending on @{link isMigrationNeeded}, this operation might takes some time to complete,
     * therefore should not be called on the main thread.
     */
    fun obtainDatabaseConnection(): DatabaseConnection
}
