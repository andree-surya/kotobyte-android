package com.kotobyte;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;

import com.kotobyte.databinding.FragmentKanjiDialogBinding;
import com.kotobyte.model.Kanji;
import com.kotobyte.util.ColorUtil;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/21.
 */
public class KanjiDialogFragment extends DialogFragment {

    public static final String TAG = KanjiDialogFragment.class.getSimpleName();

    private static final String KANJI_KEY = "kanji";

    private Kanji mKanji;

    static KanjiDialogFragment newInstance(Kanji kanji) {
        KanjiDialogFragment fragment = new KanjiDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putParcelable(KANJI_KEY, kanji);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mKanji = getArguments().getParcelable(KANJI_KEY);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = getResources().getBoolean(R.bool.bottom_sheet_enabled) ?
                new BottomSheetDialog(getContext(), getTheme()) :
                new Dialog(getActivity(), getTheme());

        dialog.setContentView(createContentView(savedInstanceState));

        return dialog;
    }

    private View createContentView(Bundle savedInstanceState) {

        FragmentKanjiDialogBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getContext()),
                R.layout.fragment_kanji_dialog,
                null,
                false);

        binding.literalTextView.setText(mKanji.getLiteral());
        binding.readingsTextView.setText(createReadingsString());
        binding.meaningsTextView.setText(createMeaningsString());

        return binding.getRoot();
    }

    private CharSequence createReadingsString() {

        StringBuilder stringBuilder = new StringBuilder();
        List<String> readings = mKanji.getReadings();

        for (int i = 0; i < readings.size(); i++) {

            stringBuilder.append(readings.get(i));

            if (i < readings.size() - 1) {
                stringBuilder.append('、');
            }
        }

        return stringBuilder.toString();
    }

    private CharSequence createMeaningsString() {

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        List<String> meanings = mKanji.getMeanings();
        List<String> extras = mKanji.getExtras();

        int meaningsStartIndex = stringBuilder.length();

        for (int i = 0; i < meanings.size(); i++) {

            stringBuilder.append(meanings.get(i));

            if (i == 0) {
                stringBuilder.setSpan(
                        new StyleSpan(Typeface.BOLD),
                        meaningsStartIndex,
                        stringBuilder.length(),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }

            if (i < meanings.size() - 1) {
                stringBuilder.append(", ");
            }
        }

        int extrasStartIndex = stringBuilder.length();

        for (int i = 0; i < extras.size(); i++) {

            if (i == 0) {
                stringBuilder.append(" ー");
            }

            stringBuilder.append(extras.get(i));

            if (i < extras.size() - 1) {
                stringBuilder.append(", ");

            } else {
                stringBuilder.setSpan(
                        new ForegroundColorSpan(ColorUtil.getColor(getContext(), R.color.light_text)),
                        extrasStartIndex,
                        stringBuilder.length(),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

        return SpannableString.valueOf(stringBuilder);
    }
}
