package com.kotobyte.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;


public class ProgressDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";

    public ProgressDialogFragment() {
        setCancelable(false);
    }

    public static ProgressDialogFragment newInstance(String message) {

        ProgressDialogFragment fragment = new ProgressDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_MESSAGE, message);

        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ProgressDialog dialog = new ProgressDialog(getContext());

        dialog.setMessage(getArguments().getString(ARG_MESSAGE));
        dialog.setIndeterminate(true);

        return dialog;
    }
}
