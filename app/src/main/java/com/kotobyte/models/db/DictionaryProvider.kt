package com.kotobyte.models.db

import android.content.res.AssetManager
import com.kotobyte.base.Configuration
import com.kotobyte.base.DatabaseConnection
import com.kotobyte.base.DatabaseProvider
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class DictionaryProvider(
        private val configuration: Configuration,
        private val assetManager: AssetManager

) : DatabaseProvider {

    private var databaseConnection: DatabaseConnection? = null

    override var isMigrationInProgress: Boolean = false
        private set

    override val isMigrationNeeded: Boolean

        get() {
            val dictionaryFileNotFound = !configuration.dictionaryFilePath.exists()

            val latestDictionaryVersion = configuration.latestDictionaryVersion
            val currentDictionaryVersion = configuration.currentDictionaryVersion

            return dictionaryFileNotFound || latestDictionaryVersion > currentDictionaryVersion
        }

    override val isMigrationPossible: Boolean

        get() {
            val freeSpace = configuration.dictionaryFilePath.parentFile.freeSpace
            val previousDictionaryFileSize = configuration.dictionaryFilePath.length()

            return freeSpace + previousDictionaryFileSize > MIN_SPACE_FOR_DICTIONARY_FILE
        }

    @Synchronized override fun obtainDatabaseConnection(): DatabaseConnection {

        if (databaseConnection == null) {

            if (isMigrationNeeded) {
                copyDatabaseFileFromAssets()
            }

            databaseConnection = DictionaryConnection(
                    configuration.dictionaryFilePath.absolutePath,
                    configuration.currentDictionaryVersion)
        }

        return databaseConnection!!
    }

    private fun copyDatabaseFileFromAssets() {

        isMigrationInProgress = true
        configuration.currentDictionaryVersion = 0

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null

        try {
            inputStream = assetManager.open(configuration.dictionaryFileName)
            outputStream = FileOutputStream(configuration.dictionaryFilePath)

            val bytesBuffer = ByteArray(inputStream!!.available())
            var numberOfBytesRead = inputStream.read(bytesBuffer)

            while (numberOfBytesRead > 0) {
                outputStream.write(bytesBuffer, 0, numberOfBytesRead)

                numberOfBytesRead = inputStream.read(bytesBuffer)
            }

            configuration.currentDictionaryVersion = configuration.latestDictionaryVersion
            isMigrationInProgress = false

        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    companion object {
        private val MIN_SPACE_FOR_DICTIONARY_FILE = 80 * 1024 * 1024
    }
}
