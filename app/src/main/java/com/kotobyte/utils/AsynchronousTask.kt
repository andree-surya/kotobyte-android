package com.kotobyte.utils

import android.os.AsyncTask

abstract class AsynchronousTask<T> : AsyncTask<Void, Void, T>() {

    private var error: Throwable? = null

    abstract fun doInBackground(): T
    abstract fun onPostExecute(data: T?, error: Throwable?)

    final override fun doInBackground(vararg params: Void?): T? {

        return try {
            doInBackground()

        } catch (error: Throwable) {
            this.error = error

            null
        }
    }

    override fun onPostExecute(data: T?) {
        onPostExecute(data, error)
    }
}