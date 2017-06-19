package com.kotobyte.base;

public class ServiceLocator {
    private static ServiceLocator sInstance;

    Configuration mConfiguration;

    public static ServiceLocator getInstance() {

        if (sInstance == null) {
            sInstance = new ServiceLocator();
        }

        return sInstance;
    }

    public Configuration getConfiguration() {
        return mConfiguration;
    }
}
