package com.example.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapplication.R
import com.example.newsapplication.databinding.FragmentSearchNewsBinding
import com.example.newsapplication.db.ArticleDatabase
import com.example.newsapplication.repository.NewsRepository
import com.example.newsapplication.util.Constants
import com.example.newsapplication.util.Resource
import com.example.newsapplication.viewModel.NewsViewModel
import com.example.newsapplication.viewModel.NewsViewModelProvider
import com.example.ui.activities.NewsActivity
import com.example.ui.adapters.NewsAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {

    lateinit var viewModel: NewsViewModel
    val TAG = "SearchNewsFragment"
    lateinit var newsAdapter: NewsAdapter
    lateinit var binding: FragmentSearchNewsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding = FragmentSearchNewsBinding.bind(view)

        val newsRepository = NewsRepository(ArticleDatabase(requireContext() as NewsActivity))
        val viewModelProviderFactory =
            NewsViewModelProvider(activity?.application!!, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        setUpRecyclerView(binding)

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment2_to_articleFragment,
                bundle
            )
        }
        var job: Job? = null
        binding.editTextSearch.addTextChangedListener { editable ->
            job?.cancel()

            job = MainScope().launch {
                delay(Constants.SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        viewModel.searchNews(editable.toString())
                    }
                }

            }

        }
        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar(binding)
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults
                        isLastPage = viewModel.searchNewsPage == totalPages
                    }
                }

                is Resource.Error -> {
                    hideProgressBar(binding)
                    response.message?.let { message ->
                        Log.e(TAG, "An error occurred: $message")
                        Toast.makeText(activity, "An error occurred: $message", Toast.LENGTH_LONG)
                            .show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar(binding)
                }

            }
        })

    }

    private fun setUpRecyclerView(binding: FragmentSearchNewsBinding) {
        newsAdapter = NewsAdapter()
        binding.searchNewsRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun hideProgressBar(binding: FragmentSearchNewsBinding) {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar(binding: FragmentSearchNewsBinding) {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = false
    }

    var isLastPage = false
    var isLoading = false
}
