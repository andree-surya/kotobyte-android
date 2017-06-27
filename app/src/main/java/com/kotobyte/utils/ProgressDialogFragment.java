package com.kotobyte.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;


public class ProgressDialogFragment extends DialogFragment {
    public static final String TAG = ProgressDialogFragment.class.getSimpleName();

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";

    public ProgressDialogFragment() {
        setCancelable(false);
    }

    public static ProgressDialogFragment newInstance(String title, String message) {

        ProgressDialogFragment fragment = new ProgressDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_TITLE, title);
        arguments.putString(ARG_MESSAGE, message);

        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        ProgressDialog dialog = new ProgressDialog(getContext());

        dialog.setTitle(getArguments().getString(ARG_TITLE));
        dialog.setMessage(getArguments().getString(ARG_MESSAGE));
        dialog.setIndeterminate(true);

        return dialog;
    }
}
