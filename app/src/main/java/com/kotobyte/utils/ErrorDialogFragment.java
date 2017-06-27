package com.kotobyte.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.kotobyte.R;

public class ErrorDialogFragment extends DialogFragment {
    public static final String TAG = ErrorDialogFragment.class.getSimpleName();

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";

    private String mTitle;
    private String mMessage;
    private Callback mCallback;

    public static ErrorDialogFragment newInstance(String title, String message) {

        ErrorDialogFragment fragment = new ErrorDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_TITLE, title);
        arguments.putString(ARG_MESSAGE, message);

        fragment.setArguments(arguments);
        return fragment;
    }

    public static ErrorDialogFragment newInstance() {
        return new ErrorDialogFragment();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getString(R.string.common_unknown_error_title);
        mMessage = getString(R.string.common_unknown_error_message);

        if (getArguments() != null) {

            if (getArguments().containsKey(ARG_TITLE)) {
                mTitle = getArguments().getString(ARG_TITLE);
            }

            if (getArguments().containsKey(ARG_MESSAGE)) {
                mMessage = getArguments().getString(ARG_MESSAGE);
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        dialogBuilder.setTitle(mTitle);
        dialogBuilder.setMessage(mMessage);

        dialogBuilder.setPositiveButton(R.string.common_okay, mOnClickListener);

        return dialogBuilder.create();
    }

    public interface Callback {
        void onClickPositiveButton();
    }

    private DialogInterface.OnClickListener mOnClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {

            if (mCallback != null) {
                mCallback.onClickPositiveButton();
            }
        }
    };
}
