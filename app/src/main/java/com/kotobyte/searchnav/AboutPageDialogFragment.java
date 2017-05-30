package com.kotobyte.searchnav;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.kotobyte.R;


public class AboutPageDialogFragment extends DialogFragment {

    public static final String TAG = AboutPageDialogFragment.class.getSimpleName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), getTheme());

        dialog.requestWindowFeature(STYLE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_about_page_dialog);

        return dialog;
    }
}
