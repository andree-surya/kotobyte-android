package com.kotobyte.utils

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.kotobyte.R

class ErrorDialogFragment : DialogFragment() {

    var callback: Callback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogBuilder = AlertDialog.Builder(context)

        dialogBuilder.setTitle(arguments?.getString(ARG_TITLE) ?: getString(R.string.common_unknown_error_title))
        dialogBuilder.setMessage(arguments?.getString(ARG_MESSAGE) ?: getString(R.string.common_unknown_error_message))

        dialogBuilder.setPositiveButton(R.string.common_okay, { _, _ ->
            callback?.onClickPositiveButton()
        })

        return dialogBuilder.create()
    }

    interface Callback {
        fun onClickPositiveButton()
    }

    companion object {
        private val ARG_TITLE = "title"
        private val ARG_MESSAGE = "message"

        fun create(title: String? = null, message: String? = null) = ErrorDialogFragment().apply {

            arguments = Bundle().apply {

                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
            }
        }
    }
}
