package com.kotobyte.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.kotobyte.R;
import com.kotobyte.base.Configuration;

import java.io.File;

public class DefaultConfiguration implements Configuration {

    private static final String KEY_CURRENT_DICTIONARY_VERSION = "current_dictionary_version";

    private SharedPreferences mSharedPreferences;
    private String mDictionaryFileName;
    private File mDictionaryFilePath;
    private int mLatestDictionaryVersion;

    public DefaultConfiguration(Context context) {
        mSharedPreferences = context.getSharedPreferences(context.getPackageName(), 0);

        mDictionaryFileName = context.getString(R.string.dictionary_file_name);
        mDictionaryFilePath = new File(context.getFilesDir(), mDictionaryFileName);
        mLatestDictionaryVersion = context.getResources().getInteger(R.integer.dictionary_version);
    }

    @Override
    public String getDictionaryFileName() {
        return mDictionaryFileName;
    }

    @Override
    public File getDictionaryFilePath() {
        return mDictionaryFilePath;
    }

    @Override
    public int getLatestDictionaryVersion() {
        return mLatestDictionaryVersion;
    }

    @Override
    public int getCurrentDictionaryVersion() {
        return mSharedPreferences.getInt(KEY_CURRENT_DICTIONARY_VERSION, 0);
    }

    @Override
    public void clearCurrentDictionaryVersion() {

        mSharedPreferences.edit()
                .remove(KEY_CURRENT_DICTIONARY_VERSION)
                .apply();
    }

    @Override
    public void updateCurrentDictionaryVersionToLatest() {

        mSharedPreferences.edit()
                .putInt(KEY_CURRENT_DICTIONARY_VERSION, mLatestDictionaryVersion)
                .apply();
    }
}
