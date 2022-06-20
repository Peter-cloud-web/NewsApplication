package com.example.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapplication.R
import com.example.newsapplication.databinding.FragmentBreakingNewsBinding
import com.example.newsapplication.util.Resource
import com.example.newsapplication.viewModel.NewsViewModel
import com.example.ui.activities.NewsActivity
import com.example.ui.adapters.NewsAdapter

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    lateinit var viewModel:NewsViewModel
    lateinit var newsAdapter:NewsAdapter
    private lateinit var binding:FragmentBreakingNewsBinding
    private val TAG = "BreakingNewsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentBreakingNewsBinding.bind(view)

        setUpRecyclerView(binding)

        viewModel = (activity as NewsActivity).viewModel

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment2_to_articleFragment,
                bundle
            )
        }

        viewModel.breakingNews.observe(viewLifecycleOwner, Observer{ response ->
            when(response){
                is Resource.Success ->{
                    hideProgressBar(binding)
                    hideErrorMessage()
                    response.data?.let{ newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles)

                    }
                }
                is Resource.Error -> {
                    hideProgressBar(binding)
                    Log.d(TAG,"inside failure")
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occured: $message", Toast.LENGTH_LONG).show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar(binding)
                }
            }

        })
        binding.btnRetry.setOnClickListener {
            viewModel.getBreakingNews("us")
        }

    }
    private fun hideProgressBar(binding:FragmentBreakingNewsBinding){
        binding.paginationProgressBar.visibility = View.INVISIBLE
    }
    private fun showProgressBar(binding:FragmentBreakingNewsBinding){
        binding.paginationProgressBar.visibility = View.VISIBLE
    }

    private fun hideErrorMessage() {
        binding.itemErrorMessage.visibility = View.INVISIBLE
        isError = false
    }
    private fun showErrorMessage(message: String) {
        binding.itemErrorMessage.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
        isError = true
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private fun setUpRecyclerView(binding:FragmentBreakingNewsBinding){
        newsAdapter = NewsAdapter()
        binding.rvBreakingNews.apply{
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }

    }

}