package com.kotobyte.search;

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

        Literal[] readings = mWords.get(position).getReadings();
        Literal[] wordLiterals = mWords.get(position).getLiterals();

        for (int i = 0; i < readings.length; i++) {
            Literal reading = readings[i];

            appendBuilderWithLiteral(builder, reading);

            if (i < readings.length - 1) {
                builder.append('、');
            }
        }

        for (int i = 0; i < wordLiterals.length; i++) {
            Literal wordLiteral = wordLiterals[i];

            if (i == 0) {
                builder.append('【');
            }

            appendBuilderWithLiteral(builder, wordLiteral);
            builder.append(i < wordLiterals.length - 1 ? '、' : '】');
        }
    }

    private void appendBuilderWithLiteral(SpannableStringBuilder builder, Literal wordLiteral) {

        int literalStartIndex = builder.length();

        appendBuilderWithHighlightableText(builder, wordLiteral.getText());

        int literalEndIndex = builder.length();

        if (wordLiteral.getPriority() == Literal.Priority.LOW) {

            builder.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(getContext(), R.color.light_text)),
                    literalStartIndex,
                    literalEndIndex,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }
}
