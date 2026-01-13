// File: app/src/main/java/ru/bloknot/news/NewsApplication.kt
package ru.bloknot.news

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ru.bloknot.news.worker.NewsUpdateWorker
import java.util.concurrent.TimeUnit

class NewsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupPeriodicNewsUpdate()
        //startTestNotificationWorker()
    }

    private fun startTestNotificationWorker() {
        val testRequest = OneTimeWorkRequestBuilder<NewsUpdateWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)   // через 5 секунд
            .addTag("test_news_notification")
            .build()

        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                "test_news_notification_once",
                ExistingWorkPolicy.REPLACE,   // перезапишет, если уже есть
                testRequest
            )
    }

    private fun setupPeriodicNewsUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)   // Только при интернете
            .setRequiresBatteryNotLow(true)                  // Не при низком заряде
            .build()

        val workRequest = PeriodicWorkRequestBuilder<NewsUpdateWorker>(
            repeatInterval = 1L,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("news_update")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "news_background_update",           // уникальное имя
            ExistingPeriodicWorkPolicy.KEEP,     // если уже запущено — не дублируем
            workRequest
        )
    }
}