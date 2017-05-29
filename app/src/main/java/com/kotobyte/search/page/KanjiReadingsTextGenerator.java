package com.kotobyte.search.page;

import android.content.Context;
import android.text.SpannableStringBuilder;

import com.kotobyte.utils.SpannableTextGenerator;
import com.kotobyte.models.Kanji;

import java.util.List;


class KanjiReadingsTextGenerator extends SpannableTextGenerator {

    private List<Kanji> mKanjiList;

    KanjiReadingsTextGenerator(Context context, List<Kanji> kanjiList) {
        super(context, kanjiList.size());

        mKanjiList = kanjiList;
    }

    @Override
    protected void createSpannableWithBuilder(SpannableStringBuilder builder, int position) {

        List<String> readings = mKanjiList.get(position).getReadings();

        for (int i = 0; i < readings.size(); i++) {

            builder.append(readings.get(i));

            if (i < readings.size() - 1) {
                builder.append('、');
            }
        }
    }
}
