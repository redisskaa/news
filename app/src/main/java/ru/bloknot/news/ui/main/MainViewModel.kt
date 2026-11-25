package ru.bloknot.news.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import ru.bloknot.news.R
import ru.bloknot.news.helpclass.NetworkCheck
import ru.bloknot.news.models.CardNews

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var loadedUrl: String? = null

    fun loadNews(url: String, category: String?) {
        if (loadedUrl == url && _uiState.value is MainUiState.Success) {
            Log.d(TAG, "Уже загружено: $url — пропускаем")
            return
        }
        loadedUrl = url
        _uiState.value = MainUiState.Loading

        viewModelScope.launch {
            try {
                if (!NetworkCheck.isNetworkConnected(getApplication())) {
                    _uiState.value = MainUiState.Error(
                        message = "Приложение не работает без интернета",
                        noInternet = true
                    )
                    return@launch
                }

                val urls = withContext(Dispatchers.IO) { parseCategoryUrls(url) }
                Log.d(TAG, "Найдено URL-ов статей: ${urls.size}")

                val news = withContext(Dispatchers.IO) { parseNews(url, category) }

                _uiState.value = if (news.isEmpty()) {
                    MainUiState.Error("Ошибка загрузки, попробуйте позже")
                } else {
                    MainUiState.Success(news = news, urls = urls)
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadNews error: $url", e)
                _uiState.value = MainUiState.Error("Ошибка загрузки: ${e.message ?: "неизвестно"}")
                FirebaseCrashlytics.getInstance().log("Пустой парсинг: url=$url, category=$category")
                FirebaseCrashlytics.getInstance().recordException(
                    RuntimeException("Парсер вернул 0 новостей для $url")
                )
            }
        }
    }

    private fun parseCategoryUrls(url: String): List<String> {
        val result = mutableListOf<String>()
        try {
            val doc = Jsoup.connect(url).get()
            val elements = doc.select("ul.bigline>li>.thumbimage>a")
            val links = elements.select("a[href]")
            val elementsFull = doc.select("a.thumbimage[href]")
            val elements1 = doc.select("div.structure-section>a.structure-section__item[href]")
            val konkursy = doc.select(".last__item>a[href]")

            if (elements.`is`("a[href]")) {
                for (link in links) result.add(link.attr("abs:href"))
                Log.d(TAG, "Сценарий 1")
            } else {
                for (link in elementsFull) result.add(link.attr("abs:href"))
                Log.d(TAG, "Сценарий 2")
            }

            if (elements1.`is`("div.structure-section>a.structure-section__item[href]")) {
                for (link in elements1) result.add(link.attr("abs:href"))
                Log.d(TAG, "Сценарий 3")
            }

            if (konkursy.`is`(".last__item>a[href]")) {
                for (link in konkursy) result.add(link.attr("abs:href"))
                Log.d(TAG, "Сценарий 4")
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseCategoryUrls error: $url", e)
        }
        return result
    }

    private fun parseNews(url: String, category: String?): List<CardNews> {
        val result = mutableListOf<CardNews>()
        try {
            val userAgent = getApplication<Application>().getString(R.string.userAgentMobile)
            val doc = Jsoup.connect(url).userAgent(userAgent).get()

            doc.select("ul.bigline>li").forEach { element ->
                val title = element.select("a.sys").text()
                val cat = element.select("a.cat").text()
                val time = element.select("span.botinfo").text()
                val description = element.getElementsByTag("p").text()
                val urlImage = "https:" + element.select("img").attr("src")
                val linkElement = element.selectFirst("a.sys")
                val linkFull = linkElement?.absUrl("href") ?: ""
                result.add(CardNews(urlImage, cat, title, time, description, linkFull))
            }

            if (result.isEmpty()) {
                doc.select("div.catitem-row>div.catitem").forEach { element ->
                    val title = element.select("a.linksys").text()
                    val time = element.select("span.botinfo").text()
                    val description = element.select("span.previewtext").text()
                    val urlImage = "https:" + element.select("img").attr("src")
                    result.add(CardNews(urlImage, category ?: "", title, time, description))
                }

                doc.select(".last__item>a").forEach { element ->
                    val time = "в разработке"
                    val title = element.getElementsByClass("last__description").select("h3").text()
                    val urlImage = "https:" + element.select("img").attr("src")
                    result.add(CardNews(urlImage, category ?: "", title, time, time))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseNews error: $url", e)
        }
        return result
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}