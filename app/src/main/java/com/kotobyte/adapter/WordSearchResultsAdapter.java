package com.kotobyte.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kotobyte.R;
import com.kotobyte.model.Kanji;
import com.kotobyte.model.KanjiSearchResults;
import com.kotobyte.model.WordSearchResults;
import com.kotobyte.view.WordLiteralsStringFactory;
import com.kotobyte.view.WordSensesStringFactory;

/**
 * Created by andree.surya on 2017/01/03.
 */
public class WordSearchResultsAdapter
        extends RecyclerView.Adapter<WordSearchResultsAdapter.ViewHolder>
        implements KanjiSearchResultsAdapter.Listener {

    private static final int NONE = -1;

    private Context mContext;
    private Listener mListener;

    private WordSearchResults mWordSearchResults;
    private SparseArray<KanjiSearchResults> mKanjiSearchResults;

    private WordLiteralsStringFactory mWordLiteralsStringFactory;
    private WordSensesStringFactory mWordSensesStringFactory;

    private int mExpandedCellPosition = NONE;

    public WordSearchResultsAdapter(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    public void setWordsSearchResults(WordSearchResults wordSearchResults) {

        mWordSearchResults = wordSearchResults;
        mKanjiSearchResults = new SparseArray<>();

        mWordLiteralsStringFactory = new WordLiteralsStringFactory(mContext, wordSearchResults.getWords());
        mWordSensesStringFactory = new WordSensesStringFactory(mContext, wordSearchResults.getWords());

        notifyDataSetChanged();
    }

    public boolean hasKanjiSearchResultsAtPosition(int position) {
        return mKanjiSearchResults.get(position) != null;
    }

    public void setKanjiSearchResults(int position, KanjiSearchResults kanjiSearchResults) {
        mKanjiSearchResults.put(position, kanjiSearchResults);

        if (position == mExpandedCellPosition) {
            notifyItemChanged(position);
        }
    }

    public int getExpandedCellPosition() {
        return mExpandedCellPosition;
    }

    public void expandCellAtPosition(int position) {

        int previouslyExpandedPosition = mExpandedCellPosition;
        mExpandedCellPosition = position;

        if (previouslyExpandedPosition == mExpandedCellPosition) {
            mExpandedCellPosition = NONE;
        }

        if (previouslyExpandedPosition != NONE) {
            notifyItemChanged(previouslyExpandedPosition);
        }

        if (mExpandedCellPosition != NONE) {
            notifyItemChanged(mExpandedCellPosition);
        }
    }

    public void collapseCellAtPosition(int position) {

        if (mExpandedCellPosition == position) {
            mExpandedCellPosition = NONE;

            notifyItemChanged(position);
        }
    }

    @Override
    public int getItemCount() {
        return mWordSearchResults != null ? mWordSearchResults.getSize() : 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.view_word_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, new KanjiSearchResultsAdapter(mContext, this));

        viewHolder.mWordContainer.setOnClickListener(new OnWordClickListener(viewHolder));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        viewHolder.mLiteralsTextView.setText(mWordLiteralsStringFactory.getSpannableString(position));
        viewHolder.mSensesTextView.setText(mWordSensesStringFactory.getSpannableString(position));

        if (position == mExpandedCellPosition) {

            viewHolder.mCardContainer.setCardElevation(
                    mContext.getResources().getDimensionPixelSize(R.dimen.expanded_card_elevation));

            KanjiSearchResults kanjiSearchResults = mKanjiSearchResults.get(position);

            if (kanjiSearchResults == null) {
                viewHolder.mProgressBar.setVisibility(View.VISIBLE);
                viewHolder.mKanjiListView.setVisibility(View.GONE);

            } else {
                viewHolder.mProgressBar.setVisibility(View.GONE);
                viewHolder.mKanjiListView.setVisibility(View.VISIBLE);
                viewHolder.mKanjiSearchResultsAdapter.setKanjiSearchResults(kanjiSearchResults);
            }

        } else {
            viewHolder.mCardContainer.setCardElevation(0);
            viewHolder.mProgressBar.setVisibility(View.GONE);
            viewHolder.mKanjiListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClickKanji(Kanji kanji) {
        mListener.onClickKanji(kanji);
    }

    public interface Listener {
        void onClickWordAtPosition(int position);
        void onClickKanji(Kanji kanji);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView mCardContainer;
        private ViewGroup mWordContainer;
        private TextView mLiteralsTextView;
        private TextView mSensesTextView;
        private ProgressBar mProgressBar;
        private RecyclerView mKanjiListView;

        private KanjiSearchResultsAdapter mKanjiSearchResultsAdapter;

        ViewHolder(View itemView, KanjiSearchResultsAdapter kanjiSearchResultsAdapter) {
            super(itemView);

            mCardContainer = (CardView) itemView.findViewById(R.id.card_container);
            mWordContainer = (ViewGroup) itemView.findViewById(R.id.word_container);
            mLiteralsTextView = (TextView) itemView.findViewById(R.id.literals_text_view);
            mSensesTextView = (TextView) itemView.findViewById(R.id.senses_text_view);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            mKanjiListView = (RecyclerView) itemView.findViewById(R.id.kanji_list_view);

            mKanjiSearchResultsAdapter = kanjiSearchResultsAdapter;
            mKanjiListView.setAdapter(kanjiSearchResultsAdapter);
        }
    }

    private class OnWordClickListener implements View.OnClickListener {

        private ViewHolder mViewHolder;

        OnWordClickListener(ViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            mListener.onClickWordAtPosition(mViewHolder.getAdapterPosition());
        }
    }
 }
