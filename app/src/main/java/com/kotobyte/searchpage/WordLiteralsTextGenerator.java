package com.kotobyte.searchpage;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.kotobyte.R;
import com.kotobyte.utils.SpannableTextGenerator;
import com.kotobyte.models.Literal;
import com.kotobyte.models.Word;

import java.util.List;


class WordLiteralsTextGenerator extends SpannableTextGenerator {

    private List<Word> mWords;

    WordLiteralsTextGenerator(Context context, List<Word> words) {
        super(context, words.size());

        mWords = words;
    }

    @Override
    protected void createSpannableWithBuilder(SpannableStringBuilder builder, int position) {

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
                    new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.light_text)),
                    literalStartIndex,
                    literalEndIndex,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }
}
