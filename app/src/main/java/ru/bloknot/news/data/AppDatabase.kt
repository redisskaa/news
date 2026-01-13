// File: app/src/main/java/ru/bloknot/news/data/AppDatabase.kt
package ru.bloknot.news.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NewsEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
}