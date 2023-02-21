package com.example.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.newsapplication.R
import com.example.newsapplication.databinding.FragmentArticleBinding
import com.example.newsapplication.db.ArticleDatabase
import com.example.newsapplication.model.FavouriteArticles
import com.example.newsapplication.repository.NewsRepository
import com.example.newsapplication.viewModel.NewsViewModel
import com.example.newsapplication.viewModel.NewsViewModelProvider
import com.example.ui.activities.NewsActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar

class ArticleFragment : Fragment(R.layout.fragment_article) {
    lateinit var viewModel: NewsViewModel
    val args: ArticleFragmentArgs by navArgs()
    private var fragmentArticleBinding: FragmentArticleBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentArticleBinding.bind(view)

        fragmentArticleBinding = binding

        val newsRepository = NewsRepository(ArticleDatabase(requireContext() as NewsActivity))
        val viewModelProviderFactory =
            NewsViewModelProvider(activity?.application!!, newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)

        val article = args.article
        binding.webView.apply {
            webViewClient = WebViewClient()
            loadUrl(article.url)

            webViewClient = object : WebViewClient() {
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

        binding.fab.setOnClickListener {
            val favouriteArticles = FavouriteArticles(
                article.id,
                article.author,
                article.content,
                article.description,
                article.publishedAt,
                article.source,
                article.title,
                article.isBookmarked,
                article.url,
                article.urlToImage
            )
            viewModel.saveArticle(favouriteArticles)
            Snackbar.make(view, "Article successfully saved", Snackbar.LENGTH_SHORT).show()
        }
    }

}

