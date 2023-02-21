package com.example.newsapplication.viewModel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapplication.model.FavouriteArticles
import com.example.newsapplication.model.NewsResponse
import com.example.newsapplication.repository.NewsRepository
import com.example.newsapplication.util.Resource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(app: Application, val newsRepository: NewsRepository) : ViewModel() {

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1
    var searchNewsResponse: NewsResponse? = null

    private val refreshTriggerChannel = Channel<Refresh>()

    private val refreshTrigger = refreshTriggerChannel.receiveAsFlow()

    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()

    private var pendingScrollToTopAfterRefresh = false

    val getBreakingNews = refreshTrigger.flatMapLatest { refresh ->
        newsRepository.getArticles(
            refresh == Refresh.FORCE,
            onFetchSuccess = {
                pendingScrollToTopAfterRefresh = true
            },
            onFetchFailed = { t ->
                viewModelScope.launch { eventChannel.send(Event.ShowErrorMessage(t)) }
            }
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onStart() {
        if (getBreakingNews.value?.status != Resource.Status.LOADING) {
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

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = resultResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.success(resultResponse)
            }
        }
        return Resource.error(response.message())
    }

    fun fetchSavedArticles() = newsRepository.getFavouriteNews()

    fun saveArticle(article: FavouriteArticles) = viewModelScope.launch {
        newsRepository.insertFavouriteArticles(article)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        val response = newsRepository.searchNews(searchQuery, searchNewsPage)
        searchNews.postValue(handleSearchNewsResponse(response))
    }
}





