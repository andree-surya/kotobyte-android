package com.kotobyte.main;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.kotobyte.R;


public class AboutPageFragment extends DialogFragment {

    public static final String TAG = AboutPageFragment.class.getSimpleName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), getTheme());

        dialog.requestWindowFeature(STYLE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_about_page);

        return dialog;
    }
}
