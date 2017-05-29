package com.kotobyte.search.page;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.kotobyte.utils.SpannableTextGenerator;
import com.kotobyte.models.Kanji;

import java.util.List;


class KanjiMeaningsTextGenerator extends SpannableTextGenerator {

    private List<Kanji> mKanjiList;

    KanjiMeaningsTextGenerator(Context context, List<Kanji> kanjiList) {
        super(context, kanjiList.size());

        mKanjiList = kanjiList;
    }

    @Override
    protected void createSpannableWithBuilder(SpannableStringBuilder builder, int position) {

        List<String> meanings = mKanjiList.get(position).getMeanings();

        for (int i = 0; i < meanings.size(); i++) {

            builder.append(meanings.get(i));

            if (i == 0) {
                builder.setSpan(
                        new StyleSpan(Typeface.BOLD),
                        0,
                        builder.length(),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }

            if (i < meanings.size() - 1) {
                builder.append(", ");
            }
        }
    }
}
