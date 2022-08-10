package com.example.newsapplication.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "articles"
)
data class Article(

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    val author: String?,
    val content: String?,
    val description: String?,
    val publishedAt: String?,
    val source: Source?,
    val title: String?,
    val isBookmarked:Boolean?,
    val url: String,
    val urlToImage: String?
) : Serializable
//{
//
//    override fun hashCode(): Int {
//        var result = id.hashCode()
//      if(author.isNullOrEmpty()){
//          result = 31 * result + author.hashCode()
//      }
//        return result
//    }
//
//}


