package com.kotobyte.base;

import android.app.Application;

import com.kotobyte.utils.RemoteDataRepository;


public class BaseApplication extends Application {
    private static final String WEB_SERVICE_URL = "http://kotobyte.com";

    @Override
    public void onCreate() {
        super.onCreate();

        ServiceLocator serviceLocator = ServiceLocator.getInstance();
        serviceLocator.mDataRepository = new RemoteDataRepository(WEB_SERVICE_URL);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (level == TRIM_MEMORY_RUNNING_LOW || level == TRIM_MEMORY_RUNNING_CRITICAL) {

            DataRepository dataRepository = ServiceLocator.getInstance().getDataRepository();

            if (dataRepository instanceof RemoteDataRepository) {
                ((RemoteDataRepository) dataRepository).clearCache();
            }
        }
    }
}
