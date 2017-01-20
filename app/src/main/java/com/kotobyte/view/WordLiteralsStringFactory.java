package com.kotobyte.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.kotobyte.R;
import com.kotobyte.model.Literal;
import com.kotobyte.model.Word;
import com.kotobyte.util.ColorUtil;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/08.
 */
public class WordLiteralsStringFactory extends SpannableStringFactory {



    private List<Word> mWords;

    public WordLiteralsStringFactory(Context context, List<Word> words) {
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

        appendBuilderWithHighlightableText(builder, literal.getText());

        int literalEndIndex = builder.length();

        if (literal.getStatus() == Literal.Status.OUTDATED ||
                literal.getStatus() == Literal.Status.IRREGULAR) {

            builder.setSpan(
                    new ForegroundColorSpan(ColorUtil.getColor(getContext(), R.color.light_text)),
                    literalStartIndex,
                    literalEndIndex,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }
}
