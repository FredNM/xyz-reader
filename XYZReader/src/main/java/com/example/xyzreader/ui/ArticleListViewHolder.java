package com.example.xyzreader.ui;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.databinding.ListItemArticleBinding;

public class ArticleListViewHolder extends RecyclerView.ViewHolder {

    public ListItemArticleBinding binding;

    public ArticleListViewHolder(View view) {
        super(view);
        binding = DataBindingUtil.bind(itemView);
    }
}