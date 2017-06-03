package com.kotobyte.searchpage;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.kotobyte.R;
import com.kotobyte.databinding.FragmentKanjiDetailDialogBinding;
import com.kotobyte.models.Kanji;
import com.kotobyte.utils.vector.VectorPathParser;


public class KanjiDetailDialogFragment extends DialogFragment {
    public static final String TAG = KanjiDetailDialogFragment.class.getSimpleName();

    private static final String KANJI_KEY = "kanji";

    static KanjiDetailDialogFragment newInstance(Kanji kanji) {
        KanjiDetailDialogFragment fragment = new KanjiDetailDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable(KANJI_KEY, kanji);
        fragment.setArguments(arguments);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = getResources().getBoolean(R.bool.bottom_sheet_enabled) ?
                new BottomSheetDialog(getContext(), getTheme()) :
                new Dialog(getActivity(), getTheme());

        dialog.requestWindowFeature(STYLE_NO_TITLE);
        dialog.setContentView(createContentView());

        return dialog;
    }

    private View createContentView() {

        FragmentKanjiDetailDialogBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.fragment_kanji_detail_dialog,
                null,
                false);

        Kanji kanji = getArguments().getParcelable(KANJI_KEY);

        if (kanji != null) {
            binding.literalTextView.setText(kanji.getLiteral());
            binding.readingsTextView.setText(new KanjiReadingsTextGenerator(getContext(), kanji).getSpannableString());
            binding.meaningsTextView.setText(new KanjiMeaningsTextGenerator(getContext(), kanji).getSpannableString());
            binding.kanjiStrokesTextureView.setVectorPaths(VectorPathParser.parse(kanji.getStrokes()));
        }

        return binding.getRoot();
    }
}
