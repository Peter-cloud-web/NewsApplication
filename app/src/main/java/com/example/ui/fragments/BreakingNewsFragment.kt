package com.example.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapplication.R
import com.example.newsapplication.databinding.FragmentBreakingNewsBinding
import com.example.newsapplication.db.ArticleDatabase
import com.example.newsapplication.repository.NewsRepository
import com.example.newsapplication.util.Resource
import com.example.newsapplication.viewModel.NewsViewModel
import com.example.newsapplication.viewModel.NewsViewModelProvider
import com.example.ui.activities.NewsActivity
import com.example.ui.adapters.BreakingNewsAdapter


class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    lateinit var viewModel: NewsViewModel
    lateinit var breakingNewsAdapter: BreakingNewsAdapter
    private lateinit var binding: FragmentBreakingNewsBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBreakingNewsBinding.bind(view)

        setUpRecyclerView(binding)

        val newsRepository = NewsRepository(ArticleDatabase(requireContext() as NewsActivity))
        val viewModelProviderFactory =
            NewsViewModelProvider(activity?.application!!, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        breakingNewsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_breakingNewsFragment2_to_articleFragment,
                bundle
            )
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getBreakingNews.collect {
                breakingNewsAdapter.differ.submitList(it?.data)
                it ?: return@collect

                showProgressBar(binding)

                when (it.status) {
                    Resource.Status.SUCCESS -> {
                        hideProgressBar(binding)
                        hideErrorMessage()
                        Toast.makeText(activity, "Success", Toast.LENGTH_LONG).show()

                    }
                    Resource.Status.LOADING -> {
                        Toast.makeText(
                            activity,
                            "Wait for data. Data is loading...",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    Resource.Status.ERROR -> {
                        showErrorMessage("Sorry!!!,Something went wrong")
                    }
                    Resource.Status.FAILURE -> {
                        showErrorMessage("Sorry!!!,Something went terribly wrong")
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
        Toast.makeText(activity, "start", Toast.LENGTH_LONG).show()
    }

    private fun hideProgressBar(binding: FragmentBreakingNewsBinding) {
        binding.paginationProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar(binding: FragmentBreakingNewsBinding) {
        binding.paginationProgressBar.visibility = View.VISIBLE
    }

    private fun hideErrorMessage() {
        binding.itemErrorMessage.visibility = View.INVISIBLE
    }

    private fun showErrorMessage(message: String) {
        binding.itemErrorMessage.visibility = View.VISIBLE
        binding.errorMessage.text = message
    }

    private fun setUpRecyclerView(binding: FragmentBreakingNewsBinding) {
        breakingNewsAdapter = BreakingNewsAdapter()
        binding.breakingNewsRecyclerView.apply {
            adapter = breakingNewsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}
