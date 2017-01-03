package com.kotobyte;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kotobyte.model.Literal;
import com.kotobyte.model.SearchResults;
import com.kotobyte.model.Sense;
import com.kotobyte.model.Word;

import java.util.List;

/**
 * Created by andree.surya on 2017/01/03.
 */
class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private static final char HIGHLIGHT_START = '{';
    private static final char HIGHLIGHT_END = '}';

    private Context mContext;
    private SearchResults mSearchResults;

    private SpannableStringBuilder mStringBuilder;
    private SparseArray<CharSequence> mLiteralsTexts;
    private SparseArray<CharSequence> mSensesTexts;

    SearchResultsAdapter(Context context) {
        mContext = context;
        mStringBuilder = new SpannableStringBuilder();
    }

    void setSearchResults(SearchResults searchResults) {
        mSearchResults = searchResults;

        mLiteralsTexts = new SparseArray<>(searchResults.getSize());
        mSensesTexts = new SparseArray<>(searchResults.getSize());

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mSearchResults != null ? mSearchResults.getSize() : 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.view_word_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        viewHolder.mLiteralsTextView.setText(getLiteralsText(position));
        viewHolder.mSensesTextView.setText(getSensesText(position));
    }

    private CharSequence getLiteralsText(int position) {

        CharSequence literalsText = mLiteralsTexts.get(position);

        if (literalsText == null) {
            Word word = mSearchResults.getWord(position);

            List<Literal> readings = word.getReadings();
            List<Literal> literals = word.getLiterals();

            for (int i = 0; i < readings.size(); i++) {
                Literal reading = readings.get(i);

                appendStringBuilderWithLiteral(mStringBuilder, reading);

                if (i < readings.size() - 1) {
                    mStringBuilder.append('、');
                }
            }

            for (int i = 0; i < literals.size(); i++) {
                Literal literal = literals.get(i);

                if (i == 0) {
                    mStringBuilder.append('【');
                }

                appendStringBuilderWithLiteral(mStringBuilder, literal);
                mStringBuilder.append(i < literals.size() - 1 ? '、' : '】');
            }

            literalsText = SpannableString.valueOf(mStringBuilder);

            mStringBuilder.clear();
            mStringBuilder.clearSpans();

            mLiteralsTexts.put(position, literalsText);
        }

        return literalsText;
    }

    private CharSequence getSensesText(int position) {

        CharSequence sensesText = mSensesTexts.get(position);

        if (sensesText == null) {
            List<Sense> senses = mSearchResults.getWord(position).getSenses();

            for (int i = 0; i < senses.size(); i++) {
                mStringBuilder.append("▸  ");

                appendStringBuilderWithText(mStringBuilder, senses.get(i).getText());

                // Extra spaces between senses.
                if (i < senses.size() - 1) {
                    mStringBuilder.append("\n\n");

                    mStringBuilder.setSpan(
                            new RelativeSizeSpan(0.25f),
                            mStringBuilder.length() - 1,
                            mStringBuilder.length(),
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }

            sensesText = SpannableString.valueOf(mStringBuilder);

            mStringBuilder.clear();
            mStringBuilder.clearSpans();

            mSensesTexts.put(position, sensesText);
        }

        return sensesText;
    }

    private void appendStringBuilderWithLiteral(SpannableStringBuilder stringBuilder, Literal literal) {
        appendStringBuilderWithText(stringBuilder, literal.getText());

        Literal.Status literalStatus = literal.getStatus();

        if (literalStatus != null) {
            int statusSymbolResource = 0;

            switch (literalStatus) {
                case COMMON:
                    statusSymbolResource = R.string.hint_common;
                    break;

                case IRREGULAR:
                    statusSymbolResource = R.string.hint_irregular;
                    break;

                case OUTDATED:
                    statusSymbolResource = R.string.hint_outdated;
                    break;
            }

            stringBuilder.append(' ');

            int startIndex = mStringBuilder.length();
            mStringBuilder.append(mContext.getString(statusSymbolResource));

            stringBuilder.setSpan(
                    new RelativeSizeSpan(0.6f),
                    startIndex,
                    stringBuilder.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            stringBuilder.append(' ');
        }
    }

    private void appendStringBuilderWithText(SpannableStringBuilder stringBuilder, String text) {
        int highlightStartIndex = -1;

        for (int j = 0; j < text.length(); j++) {
            char character = text.charAt(j);

            if (character == HIGHLIGHT_START) {
                highlightStartIndex = stringBuilder.length();

            } else if (character == HIGHLIGHT_END && highlightStartIndex >= 0) {

                stringBuilder.setSpan(
                        new BackgroundColorSpan(getColor(R.color.highlight)),
                        highlightStartIndex,
                        stringBuilder.length(),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                highlightStartIndex = -1;

            } else {
                stringBuilder.append(character);
            }
        }
    }

    private int getColor(int colorResource) {

        int highlightColor;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            highlightColor = mContext.getColor(colorResource);

        } else {
            highlightColor = mContext.getResources().getColor(colorResource);
        }

        return highlightColor;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mLiteralsTextView;
        private TextView mSensesTextView;

        ViewHolder(View itemView) {
            super(itemView);

            mLiteralsTextView = (TextView) itemView.findViewById(R.id.literals_text_view);
            mSensesTextView = (TextView) itemView.findViewById(R.id.senses_text_view);
        }
    }
 }
