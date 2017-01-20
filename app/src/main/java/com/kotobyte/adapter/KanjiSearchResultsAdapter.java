package com.kotobyte.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kotobyte.R;
import com.kotobyte.model.Kanji;
import com.kotobyte.model.KanjiSearchResults;
import com.kotobyte.view.KanjiMeaningsStringFactory;
import com.kotobyte.view.KanjiReadingsStringFactory;

/**
 * Created by andree.surya on 2017/01/18.
 */
class KanjiSearchResultsAdapter extends RecyclerView.Adapter<KanjiSearchResultsAdapter.ViewHolder> {

    private Context mContext;
    private Listener mListener;

    private KanjiSearchResults mKanjiSearchResults;

    private KanjiReadingsStringFactory mKanjiReadingsStringFactory;
    private KanjiMeaningsStringFactory mKanjiMeaningsStringFactory;

    KanjiSearchResultsAdapter(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    void setKanjiSearchResults(KanjiSearchResults kanjiSearchResults) {
        mKanjiSearchResults = kanjiSearchResults;

        mKanjiReadingsStringFactory = new KanjiReadingsStringFactory(mContext, kanjiSearchResults.getKanjiList());
        mKanjiMeaningsStringFactory = new KanjiMeaningsStringFactory(mContext, kanjiSearchResults.getKanjiList());

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.view_kanji_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.mKanjiContainer.setOnClickListener(new OnKanjiClickListener(viewHolder));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mLiteralTextView.setText(mKanjiSearchResults.getKanji(position).getLiteral());
        holder.mReadingsTextView.setText(mKanjiReadingsStringFactory.getSpannableString(position));
        holder.mMeaningsTextView.setText(mKanjiMeaningsStringFactory.getSpannableString(position));
    }

    @Override
    public int getItemCount() {
        return mKanjiSearchResults != null ? mKanjiSearchResults.getSize() : 0;
    }

    interface Listener {
        void onClickKanji(Kanji kanji);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup mKanjiContainer;
        private TextView mLiteralTextView;
        private TextView mReadingsTextView;
        private TextView mMeaningsTextView;

        ViewHolder(View itemView) {
            super(itemView);

            mKanjiContainer = (ViewGroup) itemView.findViewById(R.id.kanji_container);
            mLiteralTextView = (TextView) itemView.findViewById(R.id.literal_text_view);
            mReadingsTextView = (TextView) itemView.findViewById(R.id.readings_text_view);
            mMeaningsTextView = (TextView) itemView.findViewById(R.id.meanings_text_view);
        }
    }

    private class OnKanjiClickListener implements View.OnClickListener {

        private ViewHolder mViewHolder;

        private OnKanjiClickListener(ViewHolder viewHolder) {
            mViewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            mListener.onClickKanji(mKanjiSearchResults.getKanji(mViewHolder.getAdapterPosition()));
        }
    }
}
