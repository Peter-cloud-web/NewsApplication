package com.example.newsapplication.util

import androidx.recyclerview.widget.DiffUtil
import com.example.newsapplication.model.Article
import com.example.newsapplication.model.FavouriteArticles

class RecyclerViewDiffUtil {

    companion object{
        val differCallBack = object: DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                return oldItem == newItem
            }

        }

        val favouriteNewsDifferCallBack = object: DiffUtil.ItemCallback<FavouriteArticles>() {
            override fun areItemsTheSame(oldItem: FavouriteArticles, newItem: FavouriteArticles): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: FavouriteArticles, newItem: FavouriteArticles): Boolean {
                return oldItem == newItem
            }

        }

    }

}