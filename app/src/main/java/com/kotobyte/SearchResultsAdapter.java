package com.kotobyte;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kotobyte.model.SearchResults;
import com.kotobyte.view.LiteralsStringFactory;
import com.kotobyte.view.SensesStringFactory;

/**
 * Created by andree.surya on 2017/01/03.
 */
class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private static final int NO_CELL_SELECTION = -1;

    private Context mContext;
    private SearchResults mSearchResults;
    private int mSelectedCellIndex = NO_CELL_SELECTION;

    private LiteralsStringFactory mLiteralsStringFactory;
    private SensesStringFactory mSensesStringFactory;

    SearchResultsAdapter(Context context) {
        mContext = context;
    }

    void setSearchResults(SearchResults searchResults) {
        mSearchResults = searchResults;

        mLiteralsStringFactory = new LiteralsStringFactory(mContext, searchResults.getWords());
        mSensesStringFactory = new SensesStringFactory(mContext, searchResults.getWords());

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mSearchResults != null ? mSearchResults.getSize() : 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.view_word_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.mWordContainer.setOnClickListener(new OnWordClickListener(viewHolder));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        viewHolder.mLiteralsTextView.setText(mLiteralsStringFactory.getSpannable(position));
        viewHolder.mSensesTextView.setText(mSensesStringFactory.getSpannable(position));

        viewHolder.mCardContainer.setCardElevation(0);

        if (position == mSelectedCellIndex) {
            viewHolder.mCardContainer.setCardElevation(
                    mContext.getResources().getDimensionPixelSize(R.dimen.selected_card_elevation));
        }
    }

    private class OnWordClickListener implements View.OnClickListener {

        private ViewHolder mViewHolder;

        OnWordClickListener(ViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {

            int previouslySelectedCellIndex = mSelectedCellIndex;
            mSelectedCellIndex = mViewHolder.getAdapterPosition();

            if (previouslySelectedCellIndex == mSelectedCellIndex) {
                mSelectedCellIndex = NO_CELL_SELECTION;
            }

            if (previouslySelectedCellIndex != NO_CELL_SELECTION) {
                notifyItemChanged(previouslySelectedCellIndex);
            }

            if (mSelectedCellIndex != NO_CELL_SELECTION) {
                notifyItemChanged(mSelectedCellIndex);
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView mCardContainer;
        private ViewGroup mWordContainer;
        private TextView mLiteralsTextView;
        private TextView mSensesTextView;

        ViewHolder(View itemView) {
            super(itemView);

            mCardContainer = (CardView) itemView.findViewById(R.id.card_container);
            mWordContainer = (ViewGroup) itemView.findViewById(R.id.word_container);
            mLiteralsTextView = (TextView) itemView.findViewById(R.id.literals_text_view);
            mSensesTextView = (TextView) itemView.findViewById(R.id.senses_text_view);
        }
    }
 }
