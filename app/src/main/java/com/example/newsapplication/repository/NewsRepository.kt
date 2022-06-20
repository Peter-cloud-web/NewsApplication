package com.example.newsapplication.repository

import androidx.room.Query
import com.example.newsapplication.api.RetrofitInstance
import com.example.newsapplication.db.ArticleDatabase
import com.example.newsapplication.model.Article

class NewsRepository (  val db: ArticleDatabase){

    //First two methods fetch data from the remote api
    suspend fun getBreakingNews(countryCode:String,pageNumber:Int) =
        RetrofitInstance.api.getBreakingNews(countryCode,pageNumber)

    suspend fun searchNews(searchQuery: String,pageNumber: Int) =
        RetrofitInstance.api.searchNews(searchQuery,pageNumber)

    //These two fetch data from the room database
    suspend fun insertArticle(article:Article) =
        db.getArticleDao().insertArticle(article)

    fun getSavedNews() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article:Article){
        db.getArticleDao().deleteArticle(article)
    }


}