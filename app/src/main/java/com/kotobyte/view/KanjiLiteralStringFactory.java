package com.kotobyte.view;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import com.kotobyte.R;
import com.kotobyte.model.Kanji;
import com.kotobyte.util.ColorUtil;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/18.
 */
public class KanjiLiteralStringFactory extends SpannableStringFactory {

    private List<Kanji> mKanjiList;

    public KanjiLiteralStringFactory(Context context, List<Kanji> kanjiList) {
        super(context, kanjiList.size());

        mKanjiList = kanjiList;
    }

    @Override
    void createSpannableWithBuilder(SpannableStringBuilder builder, int position) {

        Kanji kanji = mKanjiList.get(position);

        builder.append(kanji.getLiteral());

        if (kanji.getExtras().size() > 0) {

            Object literalSpan = new LiteralSpan(null,
                    getContext().getResources().getDimensionPixelSize(R.dimen.wide_underline_margin),
                    getContext().getResources().getDimensionPixelSize(R.dimen.underline_thickness),
                    ColorUtil.getColor(getContext(), R.color.literal_common),
                    ColorUtil.getColor(getContext(), R.color.highlight));

            builder.setSpan(
                    literalSpan,
                    0,
                    builder.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }
}
