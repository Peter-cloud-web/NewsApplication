package com.example.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapplication.R
import com.example.newsapplication.databinding.FragmentBreakingNewsBinding
import com.example.newsapplication.db.ArticleDatabase
import com.example.newsapplication.repository.NewsRepository
import com.example.newsapplication.util.Constants.Companion.QUERY_PAGE_SIZE
import com.example.newsapplication.util.Resource
import com.example.newsapplication.viewModel.NewsViewModel
import com.example.newsapplication.viewModel.NewsViewModelProvider
import com.example.ui.activities.NewsActivity
import com.example.ui.adapters.NewsAdapter

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    private lateinit var binding: FragmentBreakingNewsBinding
    private val TAG = "BreakingNewsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentBreakingNewsBinding.bind(view)

        setUpRecyclerView(binding)

        val newsRepository = NewsRepository(ArticleDatabase(requireContext() as NewsActivity))
        val viewModelProviderFactory =
            NewsViewModelProvider(activity?.application!!, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment2_to_articleFragment,
                bundle
            )
        }

        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar(binding)
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles)

                    }
                }
                is Resource.Error -> {
                    hideProgressBar(binding)
                    Log.d(TAG, "inside failure")
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occured: $message", Toast.LENGTH_LONG)
                            .show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar(binding)
                }
            }

        })
        binding.retryButton.setOnClickListener {
            viewModel.getBreakingNews("us")
        }

    }

    private fun hideProgressBar(binding: FragmentBreakingNewsBinding) {
        binding.paginationProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar(binding: FragmentBreakingNewsBinding) {
        binding.paginationProgressBar.visibility = View.VISIBLE
    }

    private fun hideErrorMessage() {
        binding.itemErrorMessage.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        binding.itemErrorMessage.visibility = View.VISIBLE
        binding.errorMessage.text = message
        isError = true
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false


    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition >= 0
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling



            if (shouldPaginate) {
                isScrolling = false
            } else {
                binding.breakingNewsRecyclerView.setPadding(0, 0, 0, 0)
            }

        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun setUpRecyclerView(binding: FragmentBreakingNewsBinding) {
        newsAdapter = NewsAdapter()
        binding.breakingNewsRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }

    }
}