package com.example.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapplication.databinding.ItemArticlesBinding
import com.example.newsapplication.model.Article
import com.example.newsapplication.util.RecyclerViewDiffUtil

class NewsAdapter : RecyclerView.Adapter<NewsAdapter.MyViewHolder>() {

    inner class MyViewHolder(val binding:ItemArticlesBinding):
            RecyclerView.ViewHolder(binding.root)

    val differ = AsyncListDiffer(this, RecyclerViewDiffUtil.differCallBack)

     private var  onItemClickListener:((Article) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemArticlesBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val article = differ.currentList[position]
        with(holder){
            with(article){
                binding.apply {
                    Glide.with(itemView)
                        .load(article.urlToImage)
                        .into(articleImageView)

                    sourceTextView.text = article.source.name
                    titleTextView.text = article.title
                    descriptionTextView.text = article.description
                    publishedAtTextView.text = article.publishedAt

                    itemView.setOnClickListener{
                        onItemClickListener?.let{it(article)}
                    }

                }
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnItemClickListener(listener:(Article) -> Unit){
        onItemClickListener = listener
    }


}