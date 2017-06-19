package com.kotobyte.base;

import android.app.Application;

import com.kotobyte.utils.DefaultConfiguration;


public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ServiceLocator serviceLocator = ServiceLocator.getInstance();
        serviceLocator.mConfiguration = new DefaultConfiguration(this);
    }
}
