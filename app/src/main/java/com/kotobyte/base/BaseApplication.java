package com.kotobyte.base;

import android.app.Application;

import com.kotobyte.R;
import com.kotobyte.utils.DefaultConfiguration;
import com.kotobyte.utils.RemoteDataRepository;


public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ServiceLocator serviceLocator = ServiceLocator.getInstance();
        serviceLocator.mDataRepository = new RemoteDataRepository(getString(R.string.web_service_url));
        serviceLocator.mConfiguration = new DefaultConfiguration(this);
    }
}
