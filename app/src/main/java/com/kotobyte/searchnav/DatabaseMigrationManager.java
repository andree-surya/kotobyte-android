package com.kotobyte.searchnav;

import android.content.res.AssetManager;

import com.kotobyte.base.Configuration;
import com.kotobyte.models.db.DictionaryDatabase;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

class DatabaseMigrationManager implements SearchNavigationContracts.DatabaseMigrationManager {

    private Configuration mConfiguration;
    private AssetManager mAssetManager;
    private boolean mMigrationInProgress;

    DatabaseMigrationManager(Configuration configuration, AssetManager assetManager) {
        mConfiguration = configuration;
        mAssetManager = assetManager;
    }

    @Override
    public boolean isMigrationNeeded() {

        boolean dictionaryFileNotFound = ! mConfiguration.getDictionaryFilePath().exists();

        int latestDictionaryVersion = mConfiguration.getLatestDictionaryVersion();
        int currentDictionaryVersion = mConfiguration.getCurrentDictionaryVersion();

        return dictionaryFileNotFound || latestDictionaryVersion > currentDictionaryVersion;
        //return true;
    }

    @Override
    public boolean isMigrationInProgress() {
        return mMigrationInProgress;
    }

    @Override
    public Single<Boolean> executeMigration() {

        mMigrationInProgress = true;
        mConfiguration.clearCurrentDictionaryVersion();
        
        return Single.create(new SingleOnSubscribe<Boolean>() {

            @Override
            public void subscribe(@NonNull SingleEmitter<Boolean> e) throws Exception {

                importDatabaseFile();
                buildDatabaseIndexes();

                e.onSuccess(true);
            }

        }).doOnSuccess(new Consumer<Boolean>() {

            @Override
            public void accept(@NonNull Boolean aValue) throws Exception {
                mConfiguration.updateCurrentDictionaryVersionToLatest();
            }

        }).doAfterTerminate(new Action() {

            @Override
            public void run() throws Exception {
                mMigrationInProgress = false;
            }
        });
    }
    
    private void importDatabaseFile() throws IOException {

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

    private void buildDatabaseIndexes() throws Exception {
        String dictionaryFilePath = mConfiguration.getDictionaryFilePath().getAbsolutePath();

        try (DictionaryDatabase dictionaryDatabase = new DictionaryDatabase(dictionaryFilePath, false)) {
            dictionaryDatabase.buildIndexes();
        }
    }
}
