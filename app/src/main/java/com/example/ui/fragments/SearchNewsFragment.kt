package com.example.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapplication.R
import com.example.newsapplication.databinding.FragmentSearchNewsBinding
import com.example.newsapplication.db.ArticleDatabase
import com.example.newsapplication.repository.NewsRepository
import com.example.newsapplication.util.Constants
import com.example.newsapplication.viewModel.NewsViewModel
import com.example.newsapplication.viewModel.NewsViewModelProvider
import com.example.ui.activities.NewsActivity
import com.example.ui.adapters.BreakingNewsAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var breakingNewsAdapter: BreakingNewsAdapter
    lateinit var binding: FragmentSearchNewsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSearchNewsBinding.bind(view)

        val newsRepository = NewsRepository(ArticleDatabase(requireContext() as NewsActivity))
        val viewModelProviderFactory =
            NewsViewModelProvider(activity?.application!!, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        setUpRecyclerView(binding)

        breakingNewsAdapter.setOnItemClickListener {
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
            response.data?.let { newsResponse ->
                breakingNewsAdapter.differ.submitList(newsResponse.articles.toList())
                val totalPages = newsResponse.totalResults
                isLastPage = viewModel.searchNewsPage == totalPages
            }
        })
    }

    private fun setUpRecyclerView(binding: FragmentSearchNewsBinding) {
        breakingNewsAdapter = BreakingNewsAdapter()
        binding.searchNewsRecyclerView.apply {
            adapter = breakingNewsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
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
    var isScrolling = false

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.searchNews(binding.editTextSearch.text.toString())
                isScrolling = false

            } else {
                binding.searchNewsRecyclerView.setPadding(0, 0, 0, 0)

            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }
}
