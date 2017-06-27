package com.kotobyte.models.db;

import android.content.res.AssetManager;

import com.kotobyte.base.Configuration;
import com.kotobyte.base.DatabaseConnection;
import com.kotobyte.base.DatabaseProvider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class DictionaryProvider implements DatabaseProvider {

    private static final int MIN_SPACE_FOR_DICTIONARY_FILE = 80 * 1024 * 1024;

    private DictionaryConnection mDictionaryConnection;

    private Configuration mConfiguration;
    private AssetManager mAssetManager;

    public DictionaryProvider(Configuration configuration, AssetManager assetManager) {
        mConfiguration = configuration;
        mAssetManager = assetManager;
    }

    @Override
    public boolean isMigrationNeeded() {

        boolean dictionaryFileNotFound = ! mConfiguration.getDictionaryFilePath().exists();

        int latestDictionaryVersion = mConfiguration.getLatestDictionaryVersion();
        int currentDictionaryVersion = mConfiguration.getCurrentDictionaryVersion();

        return dictionaryFileNotFound || latestDictionaryVersion > currentDictionaryVersion;
    }

    @Override
    public boolean isMigrationPossible() {

        long freeSpace = mConfiguration.getDictionaryFilePath().getParentFile().getFreeSpace();
        long previousDictionaryFileSize = mConfiguration.getDictionaryFilePath().length();

        return (freeSpace + previousDictionaryFileSize) > MIN_SPACE_FOR_DICTIONARY_FILE;
    }

    @Override
    public synchronized DatabaseConnection getConnection() {

        if (mDictionaryConnection == null) {

            try {
                boolean isInMigration = isMigrationNeeded();

                if (isInMigration) {
                    mConfiguration.setCurrentDictionaryVersion(0);

                    copyDatabaseFileFromAssets();
                }

                String dictionaryFilePath = mConfiguration.getDictionaryFilePath().getAbsolutePath();
                int dictionaryVersion = mConfiguration.getLatestDictionaryVersion();

                DictionaryConnection dictionaryConnection =
                        new DictionaryConnection(dictionaryFilePath, dictionaryVersion);

                if (isInMigration) {
                    dictionaryConnection.buildIndexes();

                    int latestDictionaryVersion = mConfiguration.getLatestDictionaryVersion();
                    mConfiguration.setCurrentDictionaryVersion(latestDictionaryVersion);
                }

                mDictionaryConnection = dictionaryConnection;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return mDictionaryConnection;
    }

    private void copyDatabaseFileFromAssets() throws IOException {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = mAssetManager.open(mConfiguration.getDictionaryFileName());
            outputStream = new FileOutputStream(mConfiguration.getDictionaryFilePath());

            byte[] bytesBuffer = new byte[inputStream.available()];
            int numberOfBytesRead;

            while ((numberOfBytesRead = inputStream.read(bytesBuffer)) > 0) {
                outputStream.write(bytesBuffer, 0, numberOfBytesRead);
            }

        } finally {

            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
