package com.example.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapplication.databinding.ItemArticlesBinding
import com.example.newsapplication.model.FavouriteArticles
import com.example.newsapplication.util.RecyclerViewDiffUtil

class FavouriteNewsAdapter : RecyclerView.Adapter<FavouriteNewsAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding: ItemArticlesBinding) :
        RecyclerView.ViewHolder(binding.root)

    val favouriteNewsDifferCallBack =
        AsyncListDiffer(this, RecyclerViewDiffUtil.favouriteNewsDifferCallBack)
    private var onItemClickListener: ((FavouriteArticles) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemArticlesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val favouriteArticles = favouriteNewsDifferCallBack.currentList[position]
        with(holder) {
            with(favouriteArticles) {
                binding.apply {
                    Glide.with(itemView)
                        .load(favouriteArticles.urlToImage)
                        .into(articleImageView)

                    sourceTextView.text = favouriteArticles.source!!.name
                    titleTextView.text = favouriteArticles.title
                    descriptionTextView.text = favouriteArticles.description
                    publishedAtTextView.text = favouriteArticles.publishedAt

                    itemView.setOnClickListener {
                        onItemClickListener?.let { it(favouriteArticles) }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return favouriteNewsDifferCallBack.currentList.size
    }

    fun setOnItemClickListener(listener: (FavouriteArticles) -> Unit) {
        onItemClickListener = listener
    }
}