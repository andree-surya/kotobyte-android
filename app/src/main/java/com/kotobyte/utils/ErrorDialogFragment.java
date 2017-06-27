package com.kotobyte.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.kotobyte.R;

public class ErrorDialogFragment extends DialogFragment {
    public static final String TAG = ErrorDialogFragment.class.getSimpleName();

    private static final String ARG_MESSAGE = "message";

    private Callback mCallback;

    public ErrorDialogFragment() {
        setCancelable(false);
    }

    public static ErrorDialogFragment newInstance(String message) {

        ErrorDialogFragment fragment = new ErrorDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_MESSAGE, message);

        fragment.setArguments(arguments);
        return fragment;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());

        dialogBuilder.setMessage(getArguments().getString(ARG_MESSAGE));
        dialogBuilder.setPositiveButton(R.string.common_retry, mOnDialogClickListener);

        return dialogBuilder.create();
    }

    public interface Callback {
        void onClickRetryButton();
    }

    private DialogInterface.OnClickListener mOnDialogClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {

            if (mCallback != null) {
                mCallback.onClickRetryButton();
            }
        }
    };
}
