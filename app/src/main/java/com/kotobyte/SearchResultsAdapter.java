package com.kotobyte;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kotobyte.model.SearchResults;
import com.kotobyte.model.Sense;

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

        viewHolder.mContainerView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        viewHolder.mSensesTextView.setText(getSensesText(position));
    }

    private CharSequence getSensesText(int position) {

        CharSequence sensesText = mSensesTexts.get(position);

        if (sensesText == null) {
            List<Sense> senses = mSearchResults.getWord(position).getSenses();

            for (int i = 0; i < senses.size(); i++) {

                String rawText = senses.get(i).getText();
                int highlightStartIndex = -1;

                // Bullet points in the case of multiple senses.
                if (senses.size() > 1) {
                    mStringBuilder.append("▸  ");
                }

                for (int j = 0; j < rawText.length(); j++) {
                    char character = rawText.charAt(j);

                    if (character == HIGHLIGHT_START) {
                        highlightStartIndex = mStringBuilder.length();

                    } else if (character == HIGHLIGHT_END && highlightStartIndex >= 0) {

                        mStringBuilder.setSpan(
                                createHighlightSpan(),
                                highlightStartIndex,
                                mStringBuilder.length(),
                                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                        highlightStartIndex = -1;

                    } else {
                        mStringBuilder.append(character);
                    }
                }

                // Extra spaces between senses.
                if (i < senses.size() - 1) {
                    mStringBuilder.append("\n\n");

                    mStringBuilder.setSpan(
                            new RelativeSizeSpan(0.2f),
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

    private BackgroundColorSpan createHighlightSpan() {

        int highlightColor;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            highlightColor = mContext.getColor(R.color.highlight);

        } else {
            highlightColor = mContext.getResources().getColor(R.color.highlight);
        }

        return new BackgroundColorSpan(highlightColor);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup mContainerView;
        private TextView mSensesTextView;

        ViewHolder(View itemView) {
            super(itemView);

            mContainerView = (ViewGroup) itemView.findViewById(R.id.container_view);
            mSensesTextView = (TextView) itemView.findViewById(R.id.senses_text_view);
        }
    }
 }
