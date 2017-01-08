package com.kotobyte.view;

import android.content.Context;
import android.text.SpannableStringBuilder;

import com.kotobyte.model.Sense;
import com.kotobyte.model.Word;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/08.
 */
public class SenseSpannableFactory extends SpannableStringFactory {

    private List<Word> mWords;

    public SenseSpannableFactory(Context context, List<Word> words) {
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

            if (i < senses.size() - 1) {
                builder.append('\n');
            }
        }
    }
}
