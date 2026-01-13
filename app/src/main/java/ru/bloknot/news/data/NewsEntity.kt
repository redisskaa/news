package ru.bloknot.news.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUrl: String,
    val category: String,
    val title: String,
    val time: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis() // для сортировки
)