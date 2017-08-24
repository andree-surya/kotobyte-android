package com.kotobyte.main

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment

import com.kotobyte.R


class AboutPageFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?) = Dialog(activity, theme).apply {

        requestWindowFeature(DialogFragment.STYLE_NO_TITLE)
        setContentView(R.layout.fragment_about_page)
    }
}
