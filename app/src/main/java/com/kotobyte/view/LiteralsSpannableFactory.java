package com.kotobyte.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.DimenRes;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;

import com.kotobyte.R;
import com.kotobyte.model.Literal;
import com.kotobyte.model.Word;
import com.kotobyte.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andree.surya on 2017/01/08.
 */
public class LiteralsSpannableFactory extends SpannableStringFactory {

    private static final char HIGHLIGHT_START = '{';
    private static final char HIGHLIGHT_END = '}';

    private List<Word> mWords;

    public LiteralsSpannableFactory(Context context, List<Word> words) {
        super(context, words.size());
        mWords = words;
    }

    @Override
    void createSpannableWithBuilder(SpannableStringBuilder builder, int position) {

        List<Literal> readings = mWords.get(position).getReadings();
        List<Literal> literals = mWords.get(position).getLiterals();

        for (int i = 0; i < readings.size(); i++) {
            Literal reading = readings.get(i);

            appendBuilderWithLiteral(builder, reading);

            if (i < readings.size() - 1) {
                builder.append('、');
            }
        }

        for (int i = 0; i < literals.size(); i++) {
            Literal literal = literals.get(i);

            if (i == 0) {
                builder.append('【');
            }

            appendBuilderWithLiteral(builder, literal);
            builder.append(i < literals.size() - 1 ? '、' : '】');
        }
    }

    private void appendBuilderWithLiteral(SpannableStringBuilder builder, Literal literal) {

        String text = literal.getText();

        int literalStartIndex = builder.length();
        Literal.Status status = literal.getStatus();

        List<LiteralSpan.HighlightInterval> highlightIntervals = new ArrayList<>(1);
        LiteralSpan.HighlightInterval currentHighlightInterval = null;

        for (int j = 0; j < text.length(); j++) {
            char character = text.charAt(j);

            if (character == HIGHLIGHT_START) {

                currentHighlightInterval = new LiteralSpan.HighlightInterval();
                currentHighlightInterval.setStart(builder.length() - literalStartIndex);

            } else if (character == HIGHLIGHT_END && currentHighlightInterval != null) {

                currentHighlightInterval.setEnd(builder.length() - literalStartIndex);
                highlightIntervals.add(currentHighlightInterval);

                currentHighlightInterval = null;

            } else {
                builder.append(character);
            }
        }

        int literalEndIndex = builder.length();

        Object literalSpan = new LiteralSpan(
                highlightIntervals,
                getDimensionPixelSize(R.dimen.underline_margin),
                getDimensionPixelSize(R.dimen.underline_thickness),
                getColorForLiteralStatus(status),
                ColorUtil.getColor(getContext(), R.color.highlight));

        builder.setSpan(
                literalSpan,
                literalStartIndex,
                literalEndIndex,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private int getDimensionPixelSize(@DimenRes int dimenRes) {
        return getContext().getResources().getDimensionPixelSize(dimenRes);
    }

    private int getColorForLiteralStatus(Literal.Status status) {

        if (status != null) {

            switch (status) {
                case COMMON:
                    return ColorUtil.getColor(getContext(), R.color.literal_common);

                case IRREGULAR:
                    return ColorUtil.getColor(getContext(), R.color.literal_irregular);

                case OUTDATED:
                    return ColorUtil.getColor(getContext(), R.color.literal_outdated);
            }
        }

        return Color.TRANSPARENT;
    }
}
