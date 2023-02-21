package com.example.newsapplication.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsapplication.model.Article
import com.example.newsapplication.model.FavouriteArticles
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouritesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavouriteArticles(favouriteArticles: FavouriteArticles)

    @Query("SELECT * FROM favourite_articles  ORDER BY publishedAt DESC")
    fun getFavouriteArticles(): Flow<List<FavouriteArticles>>
}