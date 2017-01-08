package com.kotobyte.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.SparseArray;

import com.kotobyte.R;

/**
 * Created by andree.surya on 2017/01/08.
 */
abstract class SpannableStringFactory {

    private static final char HIGHLIGHT_START = '{';
    private static final char HIGHLIGHT_END = '}';

    private Context mContext;
    private SparseArray<Spannable> mSpannables;
    private SpannableStringBuilder mSpannableBuilder;

    SpannableStringFactory(Context context, int initialCapacity) {
        mContext = context;

        mSpannables = new SparseArray<>(initialCapacity);
        mSpannableBuilder = new SpannableStringBuilder();
    }

    Context getContext() {
        return mContext;
    }

    public Spannable getSpannable(int position) {

        Spannable spannable = mSpannables.get(position);

        if (spannable == null) {
            createSpannableWithBuilder(mSpannableBuilder, position);
            spannable = SpannableString.valueOf(mSpannableBuilder);

            mSpannableBuilder.clearSpans();
            mSpannableBuilder.clear();

            mSpannables.put(position, spannable);
        }

        return spannable;
    }

    abstract void createSpannableWithBuilder(SpannableStringBuilder builder, int position);

    void appendBuilderWithHighlightableText(SpannableStringBuilder builder, CharSequence text) {

        int startHighlightIndex = -1;

        for (int j = 0; j < text.length(); j++) {
            char character = text.charAt(j);

            if (character == HIGHLIGHT_START) {
                startHighlightIndex = builder.length();

            } else if (character == HIGHLIGHT_END && startHighlightIndex >= 0) {
                int endHighlightIndex = builder.length();

                builder.setSpan(
                        new BackgroundColorSpan(getColor(R.color.highlight)),
                        startHighlightIndex,
                        endHighlightIndex,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                startHighlightIndex = -1;

            } else {
                builder.append(character);
            }
        }
    }

    @ColorInt
    int getColor(@ColorRes int colorRes) {
        int highlightColor;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            highlightColor = mContext.getColor(colorRes);

        } else {
            highlightColor = mContext.getResources().getColor(colorRes);
        }

        return highlightColor;
    }

    @Dimension
    int getDimensionPixelSize(@DimenRes int dimenRes) {
        return mContext.getResources().getDimensionPixelSize(dimenRes);
    }
}
