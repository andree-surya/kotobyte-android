package com.kotobyte.search;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;

import com.kotobyte.R;
import com.kotobyte.models.Origin;
import com.kotobyte.models.Sense;
import com.kotobyte.utils.SpannableTextGenerator;
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

        Sense[] senses = mWords.get(position).getSenses();

        for (int i = 0; i < senses.length; i++) {
            Sense sense = senses[i];

            builder.append("▸  ");

            appendBuilderWithHighlightableText(builder, sense.getText());
            appendBuilderWithExtras(builder, sense.getExtras(), sense.getOrigins());

            if (i < senses.length - 1) {
                builder.append('\n');
            }
        }
    }

    private void appendBuilderWithExtras(
            SpannableStringBuilder builder, String[] extras, Origin[] origins) {

        String beginMarker = " ー";
        String separator = ", ";

        int extrasStartIndex = builder.length();

        for (String extra : extras) {

            if (builder.length() == extrasStartIndex) {
                builder.append(beginMarker);

            } else {
                builder.append(separator);
            }

            builder.append(extra);
        }

        for (Origin origin : origins) {

            if (builder.length() == extrasStartIndex) {
                builder.append(beginMarker);

            } else {
                builder.append(separator);
            }

            if (origin.getText() == null) {
                builder.append(getContext().getString(
                        R.string.search_origin, origin.getLanguage()));

            } else {
                builder.append(getContext().getString(
                        R.string.search_origin_with_text, origin.getLanguage(), origin.getText()));
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
