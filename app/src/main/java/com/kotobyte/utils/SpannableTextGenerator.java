package com.kotobyte.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.SparseArray;

import com.kotobyte.R;


public abstract class SpannableTextGenerator {

    private static final char HIGHLIGHT_START = '{';
    private static final char HIGHLIGHT_END = '}';

    private Context mContext;
    private SparseArray<SpannableString> mSpannableStrings;
    private SpannableStringBuilder mSpannableStringBuilder;

    protected SpannableTextGenerator(Context context, int initialCapacity) {
        mContext = context;

        mSpannableStrings = new SparseArray<>(initialCapacity);
        mSpannableStringBuilder = new SpannableStringBuilder();
    }

    protected Context getContext() {
        return mContext;
    }

    public SpannableString getSpannableString() {
        return getSpannableString(0);
    }

    public SpannableString getSpannableString(int position) {

        SpannableString spannableString = mSpannableStrings.get(position);

        if (spannableString == null) {
            createSpannableWithBuilder(mSpannableStringBuilder, position);
            spannableString = SpannableString.valueOf(mSpannableStringBuilder);

            mSpannableStringBuilder.clearSpans();
            mSpannableStringBuilder.clear();

            mSpannableStrings.put(position, spannableString);
        }

        return spannableString;
    }

    protected void appendBuilderWithHighlightableText(SpannableStringBuilder builder, CharSequence text) {

        int startHighlightIndex = -1;

        for (int j = 0; j < text.length(); j++) {
            char character = text.charAt(j);

            if (character == HIGHLIGHT_START) {
                startHighlightIndex = builder.length();

            } else if (character == HIGHLIGHT_END && startHighlightIndex >= 0) {
                int endHighlightIndex = builder.length();

                builder.setSpan(
                        new BackgroundColorSpan(ContextCompat.getColor(getContext(), R.color.highlight)),
                        startHighlightIndex,
                        endHighlightIndex,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                startHighlightIndex = -1;

            } else {
                builder.append(character);
            }
        }
    }

    protected abstract void createSpannableWithBuilder(SpannableStringBuilder builder, int position);
}
