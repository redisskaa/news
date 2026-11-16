// File: app/src/main/java/ru/bloknot/news/helpclass/SearchHelper.kt
package ru.bloknot.news.helpclass

import ru.bloknot.news.models.CardNews
import java.util.Locale

object SearchHelper {

    @JvmStatic
    fun search(query: String?, allNews: List<CardNews>): List<CardNews> {
        if (query == null || query.trim().length < 3) return emptyList()

        val lowerQuery = query.trim().lowercase(Locale.getDefault())

        return allNews.filter { news ->
            news.title.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                    news.description.lowercase(Locale.getDefault()).contains(lowerQuery) ||
                    news.category.lowercase(Locale.getDefault()).startsWith(lowerQuery)
        }
    }
}