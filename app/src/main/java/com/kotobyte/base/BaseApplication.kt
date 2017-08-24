package com.kotobyte.base

import android.app.Application

import com.kotobyte.models.db.DictionaryProvider
import com.kotobyte.utils.DefaultConfiguration


class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        ServiceLocator.configuration = DefaultConfiguration(this)
        ServiceLocator.databaseProvider = DictionaryProvider(ServiceLocator.configuration!!, assets)
    }
}
