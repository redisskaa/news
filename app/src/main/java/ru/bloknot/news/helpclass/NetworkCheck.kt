// File: app/src/main/java/ru/bloknot/news/helpclass/NetworkCheck.kt
package ru.bloknot.news.helpclass

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

object NetworkCheck {

    private const val TYPE_WIFI = 1
    private const val TYPE_MOBILE = 2
    private const val TYPE_NOT_CONNECTED = 0

    // TODO: NetworkInfo и getActiveNetworkInfo() deprecated с API 29.
    //  Заменить на ConnectivityManager.NetworkCallback — отдельным коммитом после миграции.

    @JvmStatic
    fun getConnectivityStatus(context: Context): Int {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return TYPE_NOT_CONNECTED

        val networkInfo: NetworkInfo = connectivityManager.activeNetworkInfo
            ?: return TYPE_NOT_CONNECTED

        return when (networkInfo.type) {
            ConnectivityManager.TYPE_WIFI if networkInfo.state == NetworkInfo.State.CONNECTED -> TYPE_WIFI
            ConnectivityManager.TYPE_MOBILE if networkInfo.state == NetworkInfo.State.CONNECTED -> TYPE_MOBILE
            else -> TYPE_NOT_CONNECTED
        }
    }

    @JvmStatic
    fun isNetworkConnected(context: Context): Boolean {
        val status = getConnectivityStatus(context)
        return status == TYPE_WIFI || status == TYPE_MOBILE
    }
}