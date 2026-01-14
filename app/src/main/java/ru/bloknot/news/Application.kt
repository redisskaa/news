// File: app/src/main/java/ru/bloknot/news/NewsApplication.kt
package ru.bloknot.news

import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ru.bloknot.news.worker.NewsUpdateWorker
import java.util.concurrent.TimeUnit

class NewsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        // Делаем публичный статический метод
        @JvmStatic
        fun startNewsBackgroundUpdate(context: Context) {

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<NewsUpdateWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES  // ← здесь минуты, а не секунды!
            )
                .setConstraints(constraints)
                .addTag("news_update")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "news_background_update",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        @JvmStatic
        fun stopNewsBackgroundUpdate(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("news_background_update")
        }
    }

//    fun setupPeriodicNewsUpdate() {
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)   // Только при интернете
//            .setRequiresBatteryNotLow(true)                  // Не при низком заряде
//            .build()
//
//        val workRequest = PeriodicWorkRequestBuilder<NewsUpdateWorker>(
//            repeatInterval = 30,
//            repeatIntervalTimeUnit = TimeUnit.SECONDS
//        )
//            .setConstraints(constraints)
//            .addTag("news_update")
//            .build()
//
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//            "news_background_update",           // уникальное имя
//            ExistingPeriodicWorkPolicy.KEEP,     // если уже запущено — не дублируем
//            workRequest
//        )
//    }
}