package com.example.newsapplication.repository

import androidx.room.withTransaction
import com.example.newsapplication.api.RetrofitInstance
import com.example.newsapplication.db.ArticleDatabase
import com.example.newsapplication.model.Article
import com.example.newsapplication.model.FavouriteArticles

import com.example.newsapplication.util.Resource
import com.example.newsapplication.util.networkBoundResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

class NewsRepository(val db: ArticleDatabase) {

    private val newsArticleDao = db.getArticleDao()

    //Get all news without pagination but with offline support
    fun getArticles(
        forceRefresh: Boolean,
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit
    ): Flow<Resource<List<Article>>> = networkBoundResource(

        query = {
            newsArticleDao.getAllArticles()
        },
        fetch = {
            delay(2000)
            RetrofitInstance.api.getBreakingNews().articles
        },
        saveFetchResult = { serverArticles ->
            val breakingNews = serverArticles.map { it }
            db.withTransaction {
                newsArticleDao.deleteArticle()
                newsArticleDao.insertArticle(breakingNews)
            }

        },
        shouldFetch = { cachedArticles ->
            if (forceRefresh) {
                true
            } else {
                val sortedArticles = cachedArticles.sortedBy { article ->
                    article.publishedAt
                }
                val oldestTimeStamp = sortedArticles.firstOrNull()?.id
                val needsRefresh = oldestTimeStamp == null ||
                        oldestTimeStamp < System.currentTimeMillis() -
                        TimeUnit.MINUTES.toMillis(60)
                needsRefresh
            }
        },
        onFetchSuccess = onFetchSuccess,
        onFetchFailed = { t ->
            if (t !is HttpException && t !is IOException) {
                throw t
            }
            onFetchFailed
        }

    )

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchNews(searchQuery, pageNumber)

    suspend fun insertFavouriteArticles(favouriteArticles: FavouriteArticles) =
        db.getFavouriteDao().insertFavouriteArticles(favouriteArticles)

    fun getFavouriteNews() = db.getFavouriteDao().getFavouriteArticles()
}
