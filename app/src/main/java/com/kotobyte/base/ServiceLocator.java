package com.kotobyte.base;

public class ServiceLocator {
    private static ServiceLocator sInstance;

    Configuration mConfiguration;
    DatabaseProvider mDatabaseProvider;

    public static ServiceLocator getInstance() {

        if (sInstance == null) {
            sInstance = new ServiceLocator();
        }

        return sInstance;
    }

    public Configuration getConfiguration() {
        return mConfiguration;
    }

    public DatabaseProvider getDatabaseProvider() {
        return mDatabaseProvider;
    }
}
