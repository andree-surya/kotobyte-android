package com.kotobyte.searchpage;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kotobyte.R;
import com.kotobyte.databinding.ViewKanjiItemBinding;
import com.kotobyte.models.Kanji;

import java.util.List;


class KanjiSearchResultsAdapter extends RecyclerView.Adapter<KanjiSearchResultsAdapter.ViewHolder> {

    private Context mContext;
    private Listener mListener;

    private List<Kanji> mKanjiSearchResults;

    private KanjiReadingsTextGenerator mReadingTextsGenerator;
    private KanjiMeaningsTextGenerator mMeaningTextsGenerator;

    KanjiSearchResultsAdapter(Context context, Listener listener, List<Kanji> kanjiSearchResults) {
        mContext = context;
        mListener = listener;
        mKanjiSearchResults = kanjiSearchResults;

        mReadingTextsGenerator = new KanjiReadingsTextGenerator(mContext, kanjiSearchResults);
        mMeaningTextsGenerator = new KanjiMeaningsTextGenerator(mContext, kanjiSearchResults);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ViewKanjiItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.view_kanji_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(binding);

        viewHolder.mBinding.kanjiContainer.setOnClickListener(new OnKanjiClickListener(viewHolder));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mBinding.literalTextView.setText(mKanjiSearchResults.get(position).getLiteral());
        holder.mBinding.readingsTextView.setText(mReadingTextsGenerator.getSpannableString(position));
        holder.mBinding.meaningsTextView.setText(mMeaningTextsGenerator.getSpannableString(position));
    }

    @Override
    public int getItemCount() {
        return mKanjiSearchResults != null ? mKanjiSearchResults.size() : 0;
    }

    interface Listener {
        void onClickKanji(int position, Kanji kanji);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ViewKanjiItemBinding mBinding;

        ViewHolder(ViewKanjiItemBinding binding) {
            super(binding.getRoot());

            mBinding = binding;
        }
    }

    private class OnKanjiClickListener implements View.OnClickListener {

        private ViewHolder mViewHolder;

        private OnKanjiClickListener(ViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            int position = mViewHolder.getAdapterPosition();

            mListener.onClickKanji(position, mKanjiSearchResults.get(position));
        }
    }
}
