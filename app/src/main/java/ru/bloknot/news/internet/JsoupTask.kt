// File: app/src/main/java/ru/bloknot/news/internet/JsoupTask.kt
package ru.bloknot.news.internet

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import ru.bloknot.news.R
import ru.bloknot.news.interfaces.JsoupParseCallback
import ru.bloknot.news.utils.Constants

@SuppressLint("StaticFieldLeak")
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class JsoupTask(
    private val callback: JsoupParseCallback?,
    private val context: Context
) : AsyncTask<String, Int?, Elements?>() {

    private var error: Exception? = null

    override fun onProgressUpdate(vararg values: Int?) {
        values.firstOrNull()?.let { callback?.onProgressUpdate(it) }
    }

    override fun onPreExecute() {
        callback?.onPreExecute()
    }

    override fun doInBackground(vararg params: String?): Elements? {
        val rawUrl = params.getOrNull(0) ?: Constants.BASE_URL
        val url = if (rawUrl == "null") Constants.BASE_URL else rawUrl
        val cssQuery = params.getOrNull(1) ?: "body"

        // ⚠️ Сохраняем поведение оригинала: прогресс публикуется ДО сетевого запроса.
        //    Это баг, но чиним отдельным коммитом, чтобы не смешивать с миграцией.
        for (i in 0..100) {
            publishProgress(i)
        }

        return try {
            val doc = Jsoup.connect(url)
                .userAgent(context.resources.getString(R.string.userAgentMobile))
                .get()
            doc.select(cssQuery)
        } catch (e: Exception) {
            error = e
            null
        }
    }

    override fun onPostExecute(elements: Elements?) {
        if (callback == null) return
        if (elements != null) {
            callback.onPostExecute(elements)
        } else {
            callback.onError(error ?: RuntimeException("Unknown error in JsoupTask"))
        }
    }
}