package com.kotobyte.utils

import android.content.Context

import com.kotobyte.R
import com.kotobyte.base.Configuration

import java.io.File

class DefaultConfiguration(context: Context) : Configuration {

    private val sharedPreferences = context.getSharedPreferences(context.packageName, 0)

    override val dictionaryFileName: String = context.getString(R.string.dictionary_file_name)
    override val dictionaryFilePath: File = File(context.filesDir, dictionaryFileName)
    override val latestDictionaryVersion: Int = context.resources.getInteger(R.integer.dictionary_version)

    override var currentDictionaryVersion: Int
        get() = sharedPreferences.getInt(KEY_CURRENT_DICTIONARY_VERSION, 0)
        set(version) = sharedPreferences.edit().putInt(KEY_CURRENT_DICTIONARY_VERSION, version).apply()

    companion object {
        private val KEY_CURRENT_DICTIONARY_VERSION = "current_dictionary_version"
    }
}
