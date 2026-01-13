package ru.bloknot.news.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import ru.bloknot.news.R
import ru.bloknot.news.activity.MainActivity
import ru.bloknot.news.models.CardNews
import ru.bloknot.news.utils.Constants
import java.util.concurrent.TimeUnit

class NewsUpdateWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    val list = mutableListOf<CardNews>()

    companion object {
        const val CHANNEL_ID = "new_news_channel"
        const val NOTIFICATION_ID = 1001
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val latestNews = fetchLatestNewsTitle()
            if (latestNews.isNotEmpty()) {
                val lastKnownTitle = getLastKnownTitle()
                if (latestNews != lastKnownTitle) {
                    sendNotification(latestNews)
                    saveLastKnownTitle(latestNews)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun fetchLatestNewsTitle(): String {
        val request = Request.Builder()
            .url(Constants.BASE_URL)
            .header("User-Agent", Constants.USER_AGENT_MOBILE)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return ""
            val html = response.body.string()
            val doc = Jsoup.parse(html, Constants.BASE_URL)

            val items = doc.select("article")


            items.forEach { el ->
                try {
                    val time = el.select("span").text()

                    if (isNewsFresh(time)) {
                        println("TAG: true $time")
                    } else {
                        println("TAG: false")
                    }

                } catch (e: Exception) {}
            }

            // Берём самую первую новость (самая свежая)
            return doc.select("article").firstOrNull()
                ?.select("h2")?.text()
                ?: doc.select("h2")[1].firstOrNull()?.text()
                ?: ""
        }
    }

    private fun isNewsFresh(timeText: String): Boolean {
        if (timeText.isBlank()) return false

        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()

        return when {
            timeText.contains("сегодня", ignoreCase = true) -> {
                // "сегодня в 14:27"
                val match = Regex("""\d{1,2}:\d{2}""").find(timeText)
                val timeStr = match?.value ?: return false
                val (hour, minute) = timeStr.split(":").map { it.toInt() }

                calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
                calendar.set(java.util.Calendar.MINUTE, minute)
                calendar.set(java.util.Calendar.SECOND, 0)

                val newsTime = calendar.timeInMillis
                val diffMinutes = (now - newsTime) / (1000 * 60)
                diffMinutes in 0..1  // от 0 до 10 минут назад
            }

            timeText.contains("только что", ignoreCase = true) -> true

            else -> false // вчера, позавчера, дата — игнорируем
        }
    }

    private fun getLastKnownTitle(): String {
        val prefs = applicationContext.getSharedPreferences("news_prefs", Context.MODE_PRIVATE)
        return prefs.getString("last_news_title", "") ?: ""
    }

    private fun saveLastKnownTitle(title: String) {
        val prefs = applicationContext.getSharedPreferences("news_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("last_news_title", title) }
    }

    private fun sendNotification(title: String) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle("Новая новость")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Новые новости",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        manager.notify(NOTIFICATION_ID, notification)
    }
}