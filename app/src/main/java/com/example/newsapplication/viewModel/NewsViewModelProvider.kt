package com.example.newsapplication.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.newsapplication.repository.NewsRepository
import com.example.newsapplication.util.AppApplication
import java.lang.IllegalArgumentException

class NewsViewModelProvider(val application: Application, val newsRepository: NewsRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsViewModel::class.java)){
            return NewsViewModel(application,newsRepository) as T
        }
        throw IllegalArgumentException("Unknown View Model class")
    }
}