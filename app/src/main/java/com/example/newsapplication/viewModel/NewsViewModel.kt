package com.example.newsapplication.viewModel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.AbsListView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Query
import com.example.newsapplication.model.Article
import com.example.newsapplication.model.NewsResponse
import com.example.newsapplication.repository.NewsRepository
import com.example.newsapplication.util.AppApplication
import com.example.newsapplication.util.Constants
import com.example.newsapplication.util.OfflineViewResourceHandler
import com.example.newsapplication.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import androidx.lifecycle.viewModelScope

class NewsViewModel(app: Application, val newsRepository: NewsRepository):ViewModel() {

    val searchNews = MutableLiveData<Resource<NewsResponse>>()
    var searchNewsPage = 1

    private val refreshTriggerChannel = Channel<Refresh>()

    private val refreshTrigger = refreshTriggerChannel.receiveAsFlow()

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private var pendingScrollToTopAfterRefresh = false

    val getBreakingNews = refreshTrigger.flatMapLatest { refresh ->
        newsRepository.getArticles(
            refresh == Refresh.FORCE,
            onFetchSuccess = {pendingScrollToTopAfterRefresh = true},
            onFetchFailed = { t ->
                viewModelScope.launch { eventChannel.send(Event.ShowErrorMessage(t)) }
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily,null)

    fun onStart() {
        if (getBreakingNews.value?.status!= Resource.Status.LOADING){
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.NORMAL)
            }
        }
    }

    fun onManualRefresh() {
        if (getBreakingNews.value?.status != Resource.Status.LOADING) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.FORCE)
            }
        }
    }



    enum class Refresh {
        FORCE, NORMAL
    }
    sealed class Event {
        data class ShowErrorMessage(val error: Throwable) : Event()
    }

    fun fetchSavedArticles() = newsRepository.getSavedNews()

    fun saveArticle(article: Article) = viewModelScope.launch {

        newsRepository.insertArticle(article)

    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
//        searchNews.postValue(Resource.loading())
        val response = newsRepository.searchNews(searchQuery,searchNewsPage)
        searchNews.postValue(handleBreakingNewsResponse(response))
    }

    private fun handleBreakingNewsResponse(response:Response<NewsResponse>):Resource<NewsResponse>{
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.success(resultResponse)
            }
        }
        return Resource.error(response.message())
    }


}









//    private suspend fun safeGetBreakingNewsArticleApiCall() {
//        try {
//            if (hasInternetConnection() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                breakingNews.postValue(Resource.Loading())
//                val response = newsRepository.getArticles()
//                breakingNews.postValue(handleBreakingNewsResponse(response))
//            } else {
//                breakingNews.postValue(Resource.Loading())
//            }
//        } catch (t: Throwable) {
//            when (t) {
//                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
//                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
//
//            }
//        }
//    }


//    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
//        if (response.isSuccessful) {
//            response.body()?.let { resultResponse ->
//                return Resource.Success(resultResponse)
//            }
//        }
//        return Resource.Error(response.message())
//    }
//
//    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
//        if (response.isSuccessful) {
//            response.body()?.let { resultResponse ->
//                return Resource.Success(resultResponse)
//            }
//        }
//        return Resource.Error(response.message())
//    }




//
//
//
//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun hasInternetConnection(): Boolean {
//        val connectivityManager = getApplication<AppApplication>().getSystemService(
//            Context.CONNECTIVITY_SERVICE
//        ) as ConnectivityManager
//
//        val activeNetwork = connectivityManager.activeNetwork ?: return false
//        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
//        return when {
//            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
//            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
//            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
//            else -> false
//        }
//    }
//
//        fun deleteArticle(article: Article) = viewModelScope.launch {
//            newsRepository.deleteArticle(article)
//        }
//
//
//    }




