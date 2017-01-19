package com.kotobyte.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.kotobyte.BuildConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Created by andree.surya on 2017/01/01.
 */
class AppConfig {
    private static final String LOG_TAG = AppConfig.class.getSimpleName();
    private static final String DEFAULT_PROPERTIES_NAME = "default.properties";
    private static final String DEBUG_PROPERTIES_NAME = "debug.properties";

    private static AppConfig sInstance;

    private final String mWebServiceURL;

    private AppConfig(Properties properties) {
        mWebServiceURL = properties.getProperty("WEB_SERVICE_URL");
    }

    static synchronized  AppConfig getInstance(Context context) {

        if (sInstance == null) {
            AssetManager assetManager = context.getAssets();
            Properties properties;

            try (InputStream defaultInput = assetManager.open(DEFAULT_PROPERTIES_NAME, 0)) {

                properties = new Properties();
                properties.load(defaultInput);

                if (BuildConfig.DEBUG) {

                    try (InputStream debugInput = assetManager.open(DEBUG_PROPERTIES_NAME, 0)) {
                        properties.load(debugInput);

                    } catch (FileNotFoundException e) {

                        Log.d(LOG_TAG, String.format(Locale.US,
                                "%s not found; proceeding without.", DEBUG_PROPERTIES_NAME));
                    }
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, e.getLocalizedMessage(), e);

                throw new RuntimeException(e);
            }

            sInstance = new AppConfig(properties);
        }

        return sInstance;
    }

    String getWebServiceURL() {
        return mWebServiceURL;
    }
}
