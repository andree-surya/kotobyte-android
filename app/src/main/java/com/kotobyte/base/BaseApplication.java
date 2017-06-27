package com.kotobyte.base;

import android.app.Application;

import com.kotobyte.models.db.DictionaryProvider;
import com.kotobyte.utils.DefaultConfiguration;


public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ServiceLocator services = ServiceLocator.getInstance();
        services.mConfiguration = new DefaultConfiguration(this);
        services.mDatabaseProvider = new DictionaryProvider(services.mConfiguration, getAssets());
    }
}
