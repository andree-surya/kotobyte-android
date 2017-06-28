package com.kotobyte.search;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.kotobyte.R;
import com.kotobyte.models.Kanji;
import com.kotobyte.utils.SpannableTextGenerator;

import java.util.Collections;
import java.util.List;


class KanjiMeaningsTextGenerator extends SpannableTextGenerator {

    private List<Kanji> mKanjiList;
    private boolean mShouldShowExtras;

    KanjiMeaningsTextGenerator(Context context, Kanji kanji) {
        this(context, Collections.singletonList(kanji));

        mShouldShowExtras = true;
    }

    KanjiMeaningsTextGenerator(Context context, List<Kanji> kanjiList) {
        super(context, kanjiList.size());

        mKanjiList = kanjiList;
    }

    @Override
    protected void createSpannableWithBuilder(SpannableStringBuilder builder, int position) {

        String[] meanings = mKanjiList.get(position).getMeanings();
        String[] extras = mKanjiList.get(position).getExtras();

        for (int i = 0; i < meanings.length; i++) {

            builder.append(meanings[i]);

            if (i == 0) {
                builder.setSpan(
                        new StyleSpan(Typeface.BOLD),
                        0,
                        builder.length(),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }

            if (i < meanings.length - 1) {
                builder.append(", ");
            }
        }

        if (mShouldShowExtras) {
            int extrasStartIndex = builder.length();

            for (int i = 0; i < extras.length; i++) {

                if (i == 0) {
                    builder.append(" ー");
                }

                builder.append(extras[i]);

                if (i < extras.length - 1) {
                    builder.append(", ");

                } else {
                    builder.setSpan(
                            new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.light_text)),
                            extrasStartIndex,
                            builder.length(),
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }
}
