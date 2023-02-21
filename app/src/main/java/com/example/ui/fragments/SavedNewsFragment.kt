package com.example.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapplication.R
import com.example.newsapplication.databinding.FragmentSavedNewsBinding
import com.example.newsapplication.db.ArticleDatabase
import com.example.newsapplication.model.Article
import com.example.newsapplication.repository.NewsRepository
import com.example.newsapplication.viewModel.NewsViewModel
import com.example.newsapplication.viewModel.NewsViewModelProvider
import com.example.ui.activities.NewsActivity
import com.example.ui.adapters.FavouriteNewsAdapter
import kotlinx.coroutines.launch

class SavedNewsFragment : Fragment(R.layout.fragment_saved_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var favouriteNewsAdapter: FavouriteNewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        var binding = FragmentSavedNewsBinding.bind(view)

        setUpRecyclerView(binding)

        val newsRepository = NewsRepository(ArticleDatabase(requireContext() as NewsActivity))
        val viewModelProviderFactory =
            NewsViewModelProvider(activity?.application!!, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        favouriteNewsAdapter.setOnItemClickListener {
            val favArticle = Article(
                it.id,
                it.author,
                it.content,
                it.description,
                it.publishedAt,
                it.source,
                it.title,
                it.isBookmarked,
                it.url,
                it.urlToImage
            )
            val bundle = Bundle().apply {
                putSerializable("article", favArticle)
            }
            findNavController().navigate(
                R.id.action_savedNewsFragment2_to_articleFragment,
                bundle
            )
        }

        lifecycleScope.launch {
            viewModel.fetchSavedArticles().collect {
                favouriteNewsAdapter.favouriteNewsDifferCallBack.submitList(it)
            }
        }
    }

    private fun setUpRecyclerView(binding: FragmentSavedNewsBinding) {
        favouriteNewsAdapter = FavouriteNewsAdapter()
        binding.SavedNewsRecyclerView.apply {
            adapter = favouriteNewsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

}



