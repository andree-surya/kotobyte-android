package com.kotobyte.base;

public interface DatabaseProvider {

    boolean isMigrationNeeded();
    boolean isMigrationPossible();
    boolean isMigrationInProgress();

    /**
     * Get the shared database connection.
     *
     * Depending on @{link isMigrationNeeded}, this operation might takes some time to complete,
     * therefore should not be called on the main thread.
     */
    DatabaseConnection getConnection();
}
