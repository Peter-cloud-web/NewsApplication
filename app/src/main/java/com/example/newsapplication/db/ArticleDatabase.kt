package com.example.newsapplication.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.newsapplication.db.dao.ArticleDao
import com.example.newsapplication.db.dao.FavouritesDao
import com.example.newsapplication.model.Article
import com.example.newsapplication.model.FavouriteArticles

@Database(entities = [Article::class,FavouriteArticles::class],version = 7)
@TypeConverters(Converters::class)
abstract class ArticleDatabase:RoomDatabase() {

    abstract fun getArticleDao(): ArticleDao
    abstract fun getFavouriteDao(): FavouritesDao

    companion object{
        @Volatile
        private var instance:ArticleDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance?: synchronized(LOCK){
            instance?:createDatabase(context).also{instance = it}
        }

        private fun createDatabase(context:Context) =
            Room.databaseBuilder(context.applicationContext, ArticleDatabase::class.java, "article_db.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}