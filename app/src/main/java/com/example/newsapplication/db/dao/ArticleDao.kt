package com.example.newsapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.newsapplication.model.Article
import com.example.newsapplication.model.NewsResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: List<Article>)

    @Query("SELECT * FROM articles")
    fun getAllArticles(): Flow<List<Article>>

    @Query("DELETE FROM articles")
    suspend fun deleteArticle()
}