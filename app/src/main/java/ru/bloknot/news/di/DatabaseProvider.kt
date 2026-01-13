// File: app/src/main/java/ru/bloknot/news/di/DatabaseProvider.kt
package ru.bloknot.news.di

import android.content.Context
import androidx.room.Room
import ru.bloknot.news.data.AppDatabase

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "news_database"
            ).fallbackToDestructiveMigration().build()
            INSTANCE = instance
            instance
        }
    }
}