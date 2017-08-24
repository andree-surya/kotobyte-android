package com.kotobyte.base

import java.io.File

interface Configuration {

    val dictionaryFileName: String
    val dictionaryFilePath: File

    val latestDictionaryVersion: Int
    var currentDictionaryVersion: Int
}
