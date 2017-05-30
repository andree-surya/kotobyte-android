package com.kotobyte.searchpage;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;

import com.kotobyte.R;
import com.kotobyte.utils.SpannableTextGenerator;
import com.kotobyte.models.Sense;
import com.kotobyte.models.Word;

import java.util.List;


class WordSensesTextGenerator extends SpannableTextGenerator {

    private List<Word> mWords;

    WordSensesTextGenerator(Context context, List<Word> words) {
        super(context, words.size());
        mWords = words;
    }

    @Override
    protected void createSpannableWithBuilder(SpannableStringBuilder builder, int position) {

        List<Sense> senses = mWords.get(position).getSenses();

        for (int i = 0; i < senses.size(); i++) {
            Sense sense = senses.get(i);

            builder.append("▸  ");

            appendBuilderWithHighlightableText(builder, sense.getText());
            appendBuilderWithExtras(builder, sense.getExtras());

            if (i < senses.size() - 1) {
                builder.append('\n');
            }
        }
    }

    private void appendBuilderWithExtras(SpannableStringBuilder builder, List<String> extras) {

        int extrasStartIndex = builder.length();

        for (int i = 0; i < extras.size(); i++) {

            if (i == 0) {
                builder.append(" ー");
            }

            builder.append(extras.get(i));

            if (i < extras.size() - 1) {
                builder.append(", ");
            }
        }

        int extrasEndIndex = builder.length();

        builder.setSpan(
                new TextAppearanceSpan(getContext(), R.style.Text_Light_Italic),
                extrasStartIndex,
                extrasEndIndex,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }
}
