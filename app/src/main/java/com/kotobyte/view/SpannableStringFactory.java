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
}
