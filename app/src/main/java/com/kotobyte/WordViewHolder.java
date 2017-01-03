package com.kotobyte;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kotobyte.databinding.ViewWordBinding;

/**
 * Created by andree.surya on 2017/01/03.
 */
public class WordViewHolder extends RecyclerView.ViewHolder {

    private ViewWordBinding mBinding;

    private WordViewHolder(ViewWordBinding binding) {
        super(binding.getRoot());

        mBinding = binding;
    }

    static WordViewHolder create(Context context, ViewGroup parent) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        ViewWordBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.view_word, parent, false);

        return new WordViewHolder(binding);
    }
}
