package ru.bloknot.news.interfaces

import org.jsoup.select.Elements

interface JsoupParseCallback {
    fun onProgressUpdate(percent: Int)
    fun onPreExecute()
    fun onPostExecute(result: Elements)
    fun onError(e: Exception)
}