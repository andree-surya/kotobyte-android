package com.kotobyte.base;

public class ServiceLocator {
    private static ServiceLocator sInstance;

    DataRepository mDataRepository;

    public static ServiceLocator getInstance() {

        if (sInstance == null) {
            sInstance = new ServiceLocator();
        }

        return sInstance;
    }

    public DataRepository getDataRepository() {
        return mDataRepository;
    }
}
