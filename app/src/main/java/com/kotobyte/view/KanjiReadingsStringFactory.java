package com.kotobyte.view;

import android.content.Context;
import android.text.SpannableStringBuilder;

import com.kotobyte.model.Kanji;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/18.
 */
public class KanjiReadingsStringFactory extends SpannableStringFactory {

    private List<Kanji> mKanjiList;

    public KanjiReadingsStringFactory(Context context, List<Kanji> kanjiList) {
        super(context, kanjiList.size());

        mKanjiList = kanjiList;
    }

    @Override
    void createSpannableWithBuilder(SpannableStringBuilder builder, int position) {

        List<String> readings = mKanjiList.get(position).getReadings();

        for (int i = 0; i < readings.size(); i++) {

            builder.append(readings.get(i));

            if (i < readings.size() - 1) {
                builder.append('、');
            }
        }
    }
}
