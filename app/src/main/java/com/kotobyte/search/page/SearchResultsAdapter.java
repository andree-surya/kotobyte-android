package com.kotobyte.search.page;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kotobyte.R;
import com.kotobyte.databinding.ViewWordItemCollapsedBinding;
import com.kotobyte.databinding.ViewWordItemExpandedBinding;
import com.kotobyte.models.Kanji;
import com.kotobyte.models.Word;

import java.util.List;


class SearchResultsAdapter extends RecyclerView.Adapter {

    private static final int COLLAPSED_ITEM = 1;
    private static final int EXPANDED_ITEM = 2;
    private static final int NONE = -1;

    private Context mContext;
    private Listener mListener;

    private int mExpandedItemPosition = NONE;
    private List<Word> mWordSearchResults;
    private SparseArray<KanjiSearchResultsAdapter> mKanjiSearchResultsAdapters;

    private WordLiteralsTextGenerator mLiteralsTextGenerator;
    private WordSensesTextGenerator mSensesTextGenerator;

    SearchResultsAdapter(Context context, Listener listener, List<Word> wordSearchResults) {
        mContext = context;
        mListener = listener;

        mWordSearchResults = wordSearchResults;
        mKanjiSearchResultsAdapters = new SparseArray<>();

        mLiteralsTextGenerator = new WordLiteralsTextGenerator(context, mWordSearchResults);
        mSensesTextGenerator = new WordSensesTextGenerator(context, mWordSearchResults);

        setHasStableIds(true);
    }

    void setKanjiSearchResults(int position, List<Kanji> kanjiSearchResults) {

        mKanjiSearchResultsAdapters.put(position,
                new KanjiSearchResultsAdapter(mContext, mKanjiAdapterListener, kanjiSearchResults));

        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mWordSearchResults.size();
    }

    @Override
    public long getItemId(int position) {
        return mWordSearchResults.get(position).getID();
    }

    @Override
    public int getItemViewType(int position) {
        return mExpandedItemPosition == position ? EXPANDED_ITEM : COLLAPSED_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == EXPANDED_ITEM) {
            ViewWordItemExpandedBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()), R.layout.view_word_item_expanded, parent, false);

            RecyclerView.ViewHolder viewHolder = new ExpandedViewHolder(binding);
            binding.wordItem.wordContainer.setOnClickListener(new OnWordClickListener(viewHolder));

            return viewHolder;

        } else {
            ViewWordItemCollapsedBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()), R.layout.view_word_item_collapsed, parent, false);

            RecyclerView.ViewHolder viewHolder = new CollapsedViewHolder(binding);
            binding.wordItem.wordContainer.setOnClickListener(new OnWordClickListener(viewHolder));

            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (getItemViewType(position) == EXPANDED_ITEM) {
            ExpandedViewHolder viewHolder = (ExpandedViewHolder) holder;

            viewHolder.mBinding.wordItem.literalsTextView.setText(mLiteralsTextGenerator.getSpannableString(position));
            viewHolder.mBinding.wordItem.sensesTextView.setText(mSensesTextGenerator.getSpannableString(position));

            viewHolder.mBinding.progressBar.setVisibility(View.GONE);
            viewHolder.mBinding.kanjiListView.setVisibility(View.GONE);

            KanjiSearchResultsAdapter kanjiSearchResultsAdapter = mKanjiSearchResultsAdapters.get(position);

            if (kanjiSearchResultsAdapter == null) {
                viewHolder.mBinding.progressBar.setVisibility(View.VISIBLE);

            } else {
                viewHolder.mBinding.kanjiListView.setVisibility(View.VISIBLE);
                viewHolder.mBinding.kanjiListView.setAdapter(kanjiSearchResultsAdapter);
            }

        } else {
            CollapsedViewHolder viewHolder = (CollapsedViewHolder) holder;

            viewHolder.mBinding.wordItem.literalsTextView.setText(mLiteralsTextGenerator.getSpannableString(position));
            viewHolder.mBinding.wordItem.sensesTextView.setText(mSensesTextGenerator.getSpannableString(position));
        }
    }

    private KanjiSearchResultsAdapter.Listener mKanjiAdapterListener = new KanjiSearchResultsAdapter.Listener() {

        @Override
        public void onClickKanji(int position, Kanji kanji) {
            mListener.onRequestDetailForKanji(kanji);
        }
    };

    interface Listener {
        void onRequestKanjiListForWord(int position, Word word);
        void onRequestDetailForKanji(Kanji kanji);
    }

    private static class ExpandedViewHolder extends RecyclerView.ViewHolder {

        private ViewWordItemExpandedBinding mBinding;

        ExpandedViewHolder(ViewWordItemExpandedBinding binding) {
            super(binding.getRoot());

            mBinding = binding;
        }
    }

    private static class CollapsedViewHolder extends RecyclerView.ViewHolder {

        private ViewWordItemCollapsedBinding mBinding;

        CollapsedViewHolder(ViewWordItemCollapsedBinding binding) {
            super(binding.getRoot());

            mBinding = binding;
        }
    }

    private class OnWordClickListener implements View.OnClickListener {

        private RecyclerView.ViewHolder mViewHolder;

        OnWordClickListener(RecyclerView.ViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            int position = mViewHolder.getAdapterPosition();

            if (mExpandedItemPosition == position) {
                mExpandedItemPosition = NONE;

                notifyItemChanged(position);

            } else {
                int lastExpandedItemPosition = mExpandedItemPosition;

                mExpandedItemPosition = position;

                if (lastExpandedItemPosition != NONE) {
                    notifyItemChanged(lastExpandedItemPosition);
                }

                notifyItemChanged(mExpandedItemPosition);

                if (mKanjiSearchResultsAdapters.get(position) == null) {
                    mListener.onRequestKanjiListForWord(position, mWordSearchResults.get(position));
                }
            }
        }
    }
 }
