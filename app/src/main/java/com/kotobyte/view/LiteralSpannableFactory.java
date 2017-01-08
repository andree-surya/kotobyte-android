package com.kotobyte.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.kotobyte.R;
import com.kotobyte.model.Literal;
import com.kotobyte.model.Word;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/08.
 */
public class LiteralSpannableFactory extends SpannableStringFactory {

    private List<Word> mWords;

    public LiteralSpannableFactory(Context context, List<Word> words) {
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

        int literalStartIndex = builder.length();
        Literal.Status status = literal.getStatus();

        appendBuilderWithHighlightableText(builder, literal.getText());

        if (status != null) {
            int literalEndIndex = builder.length();

            Object styleableUnderlineSpan = new StyleableUnderlineSpan(
                    getColorForLiteralStatus(status),
                    getDimensionPixelSize(R.dimen.underline_thickness),
                    getDimensionPixelSize(R.dimen.underline_margin));

            builder.setSpan(
                    styleableUnderlineSpan,
                    literalStartIndex,
                    literalEndIndex,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }

    @ColorInt
    private int getColorForLiteralStatus(Literal.Status status) {

        switch (status) {
            case COMMON:
                return getColor(R.color.literal_common);

            case IRREGULAR:
                return getColor(R.color.literal_irregular);

            case OUTDATED:
                return getColor(R.color.literal_outdated);

            default:
                return Color.TRANSPARENT;
        }
    }
}
