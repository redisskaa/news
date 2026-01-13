package ru.bloknot.news.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import ru.bloknot.news.models.CardNews
import ru.bloknot.news.utils.Constants
import java.util.concurrent.TimeUnit

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    init {
        loadNews()
    }

    fun loadNews() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
                Log.e("HOME_VM", "Запуск загрузки новостей")
                val news = fetchAndParse()
                Log.e("HOME_VM", "УСПЕХ! Спарсили ${news.size} новостей")
                _uiState.value = HomeUiState.Success(news)
            } catch (e: Exception) {
                Log.e("HOME_VM", "КРИТИЧЕСКАЯ ОШИБКА", e)
                _uiState.value = HomeUiState.Error("Ошибка: ${e.message ?: "неизвестно"}")
            }
        }
    }

    private suspend fun fetchAndParse(): List<CardNews> = withContext(Dispatchers.IO) {

        val request = Request.Builder()
            .url(Constants.BASE_URL)
            .header("User-Agent", Constants.USER_AGENT_MOBILE)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "ru-RU,ru;q=0.9")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            val html = response.body.string()

            val doc = Jsoup.parse(html, Constants.BASE_URL)
            val list = mutableListOf<CardNews>()

            // 1. Пробуем десктопную версию (ul.bigline)
            var items = doc.select("ul.bigline > li")
            if (items.isNotEmpty()) {
                items.forEach { li ->
                    try {
                        val title = li.selectFirst("a.sys")?.ownText() ?: li.selectFirst("a.sys")?.text() ?: ""
                        val category = li.selectFirst("a.cat")?.text() ?: "Новости"
                        val time = li.selectFirst("span.botinfo")?.text() ?: ""
                        val desc = li.selectFirst("p")?.text() ?: ""
                        val img = li.selectFirst("img")?.absUrl("src") ?: li.selectFirst("img")?.attr("data-src") ?: ""
                        val linkFull = li.selectFirst("a.sys")?.absUrl("href") ?: ""

                        println("paramsHome $linkFull")

                        if (title.isNotBlank()) list.add(CardNews(img, category, title, time, desc, linkFull))
                    } catch (e: Exception) {}
                }
                return@withContext list
            }

            items = doc.select("article")

            items.forEach { el ->
                try {
                    val titleEl = el.select("h2")
                    val title = titleEl.text()
                    val descr = el.select("h2")[1].text()
                    val category = el.selectFirst("a")?.text()
                    val img = el.selectFirst("img")?.absUrl("src") ?:
                        el.selectFirst("img")?.attr("data-src") ?: ""

                    val time = el.select("span").text()
                    val linkFull = el.select("a")[1]?.absUrl("href")
                    println("paramsHome2 $linkFull")
                    if (title.isNotBlank()) {
                        list.add(CardNews(img, category, title, time, descr, linkFull))
                    }
                } catch (e: Exception) {}
            }

            return@withContext list
        }
    }
}

