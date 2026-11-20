// File: app/src/main/java/ru/bloknot/news/helpclass/NetworkCheck.kt
package ru.bloknot.news.helpclass

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkCheck {

    /**
     * true, если есть активная сеть с доступом в интернет.
     *
     * Использует NetworkCapabilities (API 23+) вместо устаревшего NetworkInfo (deprecated с API 29).
     * NET_CAPABILITY_VALIDATED отсекает Wi-Fi без выхода в интернет
     * (подключён к роутеру, но интернета нет).
     */
    @JvmStatic
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}