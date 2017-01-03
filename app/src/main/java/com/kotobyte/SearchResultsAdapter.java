package com.kotobyte;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.kotobyte.model.SearchResults;
import com.kotobyte.model.Word;

/**
 * Created by andree.surya on 2017/01/03.
 */
class SearchResultsAdapter extends RecyclerView.Adapter<WordViewHolder> {

    private Context mContext;
    private SearchResults mSearchResults;

    SearchResultsAdapter(Context context) {
        mContext = context;
    }

    void setSearchResults(SearchResults searchResults) {
        mSearchResults = searchResults;

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mSearchResults != null ? mSearchResults.getSize() : 0;
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        WordViewHolder viewHolder = WordViewHolder.create(mContext, parent);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WordViewHolder viewHolder, int position) {
        Word word = mSearchResults.getWord(position);
    }
}
