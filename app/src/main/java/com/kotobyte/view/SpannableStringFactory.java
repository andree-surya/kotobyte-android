package com.kotobyte.view;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.SparseArray;

/**
 * Created by andree.surya on 2017/01/08.
 */
abstract class SpannableStringFactory {

    private Context mContext;
    private SparseArray<SpannableString> mSpannableStrings;
    private SpannableStringBuilder mSpannableStringBuilder;

    SpannableStringFactory(Context context, int initialCapacity) {
        mContext = context;

        mSpannableStrings = new SparseArray<>(initialCapacity);
        mSpannableStringBuilder = new SpannableStringBuilder();
    }

    Context getContext() {
        return mContext;
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

    abstract void createSpannableWithBuilder(SpannableStringBuilder builder, int position);
}
