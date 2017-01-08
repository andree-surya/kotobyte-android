package com.kotobyte;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kotobyte.model.SearchResults;
import com.kotobyte.view.LiteralsSpannableFactory;
import com.kotobyte.view.SensesSpannableFactory;

/**
 * Created by andree.surya on 2017/01/03.
 */
class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private Context mContext;
    private SearchResults mSearchResults;

    private LiteralsSpannableFactory mLiteralsSpannableFactory;
    private SensesSpannableFactory mSensesSpannableFactory;

    SearchResultsAdapter(Context context) {
        mContext = context;
    }

    void setSearchResults(SearchResults searchResults) {
        mSearchResults = searchResults;

        mLiteralsSpannableFactory = new LiteralsSpannableFactory(mContext, searchResults.getWords());
        mSensesSpannableFactory = new SensesSpannableFactory(mContext, searchResults.getWords());

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

        viewHolder.mLiteralsTextView.setText(mLiteralsSpannableFactory.getSpannable(position));
        viewHolder.mSensesTextView.setText(mSensesSpannableFactory.getSpannable(position));
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
