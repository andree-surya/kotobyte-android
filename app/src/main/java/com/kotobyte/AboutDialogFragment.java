package com.kotobyte;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by andree.surya on 2017/01/22.
 */
public class AboutDialogFragment extends DialogFragment {

    public static final String TAG = AboutDialogFragment.class.getSimpleName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), getTheme());

        dialog.requestWindowFeature(STYLE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_about_dialog);

        return dialog;
    }
}
