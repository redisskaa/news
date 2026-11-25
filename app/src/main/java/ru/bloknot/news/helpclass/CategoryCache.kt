package ru.bloknot.news.helpclass

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

data class CachedCategory(val title: String, val url: String)
data class CategoryCacheResult(val items: List<CachedCategory>, val timestamp: Long)

object CategoryCache {
    private const val TAG = "CategoryCache"
    private const val PREFS = "category_cache"
    private const val KEY_ITEMS = "items"
    private const val KEY_TIMESTAMP = "timestamp"
    private const val TTL_MS = 24 * 60 * 60 * 1000L // 24 часа

    fun save(context: Context, items: List<CachedCategory>) {
        try {
            val arr = JSONArray()
            items.forEach { item ->
                arr.put(JSONObject().apply {
                    put("title", item.title)
                    put("url", item.url)
                })
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit {
                putString(KEY_ITEMS, arr.toString())
                putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            }
            Log.d(TAG, "Saved ${items.size} categories")
        } catch (e: Exception) {
            Log.e(TAG, "save error", e)
        }
    }

    fun load(context: Context): CategoryCacheResult? {
        return try {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_ITEMS, null) ?: return null
            val ts = prefs.getLong(KEY_TIMESTAMP, 0)
            val arr = JSONArray(json)
            val items = (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                CachedCategory(obj.getString("title"), obj.getString("url"))
            }
            if (items.isEmpty()) null else CategoryCacheResult(items, ts)
        } catch (e: Exception) {
            Log.e(TAG, "load error", e)
            null
        }
    }

    fun isFresh(timestamp: Long): Boolean =
        (System.currentTimeMillis() - timestamp) < TTL_MS
}