package com.kotobyte.utils

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

class ProgressDialogFragment : DialogFragment() {

    init {
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = ProgressDialog(context).apply {

        setTitle(arguments.getString(ARG_TITLE))
        setMessage(arguments.getString(ARG_MESSAGE))

        isIndeterminate = true
    }

    companion object {
        private val ARG_TITLE = "title"
        private val ARG_MESSAGE = "message"

        fun create(title: String, message: String) = ProgressDialogFragment().apply {

            arguments = Bundle().apply {

                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
            }
        }
    }
}
