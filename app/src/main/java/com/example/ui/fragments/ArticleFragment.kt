package com.example.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.newsapplication.R
import com.example.newsapplication.databinding.FragmentArticleBinding
import com.example.newsapplication.databinding.FragmentBreakingNewsBinding
import com.example.newsapplication.db.ArticleDatabase
import com.example.newsapplication.repository.NewsRepository
import com.example.newsapplication.viewModel.NewsViewModel
import com.example.newsapplication.viewModel.NewsViewModelProvider
import com.example.ui.activities.NewsActivity
import com.google.android.material.snackbar.Snackbar
import java.util.Map.of

class ArticleFragment : Fragment(R.layout.fragment_article) {
    lateinit var viewModel:NewsViewModel
    val args: ArticleFragmentArgs by navArgs()
    private var fragmentArticleBinding : FragmentArticleBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentArticleBinding.bind(view)
        fragmentArticleBinding = binding

//        val newsRepository = NewsRepository(ArticleDatabase(requireContext() as NewsActivity))
//        viewModel = (activity as NewsActivity).viewModel
        val newsRepository = NewsRepository(ArticleDatabase(requireContext() as NewsActivity))
        val viewModelProviderFactory = NewsViewModelProvider(activity?.application!!,newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)


        val article = args.article
        binding.webView.apply {
            webViewClient = WebViewClient()
            loadUrl(article.url)

            webViewClient = object:WebViewClient(){
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
        binding.fab.setOnClickListener{
            viewModel.saveArticle(article)
            Snackbar.make(view, "Article successfully saved", Snackbar.LENGTH_SHORT).show()
        }

    }

}