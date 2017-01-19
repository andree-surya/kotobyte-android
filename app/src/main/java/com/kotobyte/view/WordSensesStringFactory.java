package com.kotobyte.view;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.TextAppearanceSpan;

import com.kotobyte.R;
import com.kotobyte.model.Sense;
import com.kotobyte.model.Word;
import com.kotobyte.util.ColorUtil;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/08.
 */
public class WordSensesStringFactory extends SpannableStringFactory {

    private static final char HIGHLIGHT_START = '{';
    private static final char HIGHLIGHT_END = '}';

    private List<Word> mWords;

    public WordSensesStringFactory(Context context, List<Word> words) {
        super(context, words.size());
        mWords = words;
    }

    @Override
    void createSpannableWithBuilder(SpannableStringBuilder builder, int position) {

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

    private void appendBuilderWithHighlightableText(SpannableStringBuilder builder, CharSequence text) {

        int startHighlightIndex = -1;

        for (int j = 0; j < text.length(); j++) {
            char character = text.charAt(j);

            if (character == HIGHLIGHT_START) {
                startHighlightIndex = builder.length();

            } else if (character == HIGHLIGHT_END && startHighlightIndex >= 0) {
                int endHighlightIndex = builder.length();

                builder.setSpan(
                        new BackgroundColorSpan(ColorUtil.getColor(getContext(), R.color.highlight)),
                        startHighlightIndex,
                        endHighlightIndex,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                startHighlightIndex = -1;

            } else {
                builder.append(character);
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
