package com.kotobyte.search;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.kotobyte.R;
import com.kotobyte.databinding.FragmentKanjiDetailBinding;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (! isBottomSheetEnabled()) {
            return createContentView();
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (isBottomSheetEnabled()) {
            BottomSheetDialog dialog = new BottomSheetDialog(getContext(), getTheme());

            dialog.requestWindowFeature(STYLE_NO_TITLE);
            dialog.setContentView(createContentView());

            return dialog;
        }

        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (! isBottomSheetEnabled()) {
            Window window = getDialog().getWindow();

            if (window != null) {
                float minWidth = getResources().getDimension(R.dimen.kanji_detail_min_width);

                WindowManager.LayoutParams windowAttributes = window.getAttributes();

                if (windowAttributes.width < minWidth) {
                    windowAttributes.width = (int) minWidth;
                    window.setAttributes(windowAttributes);
                }
            }
        }
    }

    private View createContentView() {

        FragmentKanjiDetailBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.fragment_kanji_detail,
                null,
                false);

        Kanji kanji = getArguments().getParcelable(KANJI_KEY);

        if (kanji != null) {
            binding.literalTextView.setText(kanji.getCharacter());
            binding.readingsTextView.setText(new KanjiReadingsTextGenerator(getContext(), kanji).getSpannableString());
            binding.meaningsTextView.setText(new KanjiMeaningsTextGenerator(getContext(), kanji).getSpannableString());
            binding.kanjiStrokesTextureView.setVectorPaths(VectorPathParser.parse(kanji.getStrokes()));
        }

        return binding.getRoot();
    }

    private boolean isBottomSheetEnabled() {
        return getResources().getBoolean(R.bool.bottom_sheet_enabled);
    }
}
