package com.kotobyte.view;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;

import com.kotobyte.R;
import com.kotobyte.model.Sense;
import com.kotobyte.model.Word;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/08.
 */
public class SensesSpannableFactory extends SpannableStringFactory {

    private List<Word> mWords;

    public SensesSpannableFactory(Context context, List<Word> words) {
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


            appendBuilderWithSenseMetadata(builder, sense);

            if (i < senses.size() - 1) {
                builder.append('\n');
            }
        }
    }

    private void appendBuilderWithSenseMetadata(SpannableStringBuilder builder, Sense sense) {

        int metadataStartIndex = builder.length();

        List<String> categories = sense.getCategories();
        List<String> extras = sense.getExtras();

        builder.append(" [");

        for (int i = 0; i < categories.size(); i++) {
            builder.append(categories.get(i));

            if (i < categories.size() - 1) {
                builder.append(", ");
            }
        }

        builder.append("] ");

        for (int i = 0; i < extras.size(); i++) {

            if (i == 0) {
                builder.append('ー');
            }

            builder.append(extras.get(i));

            if (i < extras.size() - 1) {
                builder.append(", ");
            }
        }

        int metadataEndIndex = builder.length();

        builder.setSpan(
                new TextAppearanceSpan(getContext(), R.style.Text_Light),
                metadataStartIndex,
                metadataEndIndex,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }
}
