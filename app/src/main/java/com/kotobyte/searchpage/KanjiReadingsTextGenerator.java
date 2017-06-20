package com.kotobyte.searchpage;

import android.content.Context;
import android.text.SpannableStringBuilder;

import com.kotobyte.utils.SpannableTextGenerator;
import com.kotobyte.models.Kanji;

import java.util.Collections;
import java.util.List;


class KanjiReadingsTextGenerator extends SpannableTextGenerator {

    private List<Kanji> mKanjiList;

    KanjiReadingsTextGenerator(Context context, Kanji kanji) {
        this(context, Collections.singletonList(kanji));
    }

    KanjiReadingsTextGenerator(Context context, List<Kanji> kanjiList) {
        super(context, kanjiList.size());

        mKanjiList = kanjiList;
    }

    @Override
    protected void createSpannableWithBuilder(SpannableStringBuilder builder, int position) {

        String[] readings = mKanjiList.get(position).getReadings();

        for (int i = 0; i < readings.length; i++) {

            builder.append(readings[i]);

            if (i < readings.length - 1) {
                builder.append('、');
            }
        }
    }
}
