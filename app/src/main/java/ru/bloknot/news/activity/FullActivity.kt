// File: FullActivity.kt
package ru.bloknot.news.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import ru.bloknot.news.R
import ru.bloknot.news.utils.Constants
import java.io.IOException

class FullActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private val client = OkHttpClient()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        // Отключаем JS — нам он не нужен, только тормозит
        webView.settings.javaScriptEnabled = false
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        val url = intent.getStringExtra("url") ?: run {
            Toast.makeText(this, "Ошибка: нет ссылки", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val title = intent.getStringExtra("title") ?: run {
            Toast.makeText(this, "Ошибка: нет тайтла", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.visibility = View.VISIBLE

                progressBar.progress = newProgress // обновляем ProgressBar
                setTitle("Загрузка $newProgress %")

                if (newProgress == 100) {
                    setTitle(title)
                    progressBar.visibility = View.GONE      // скрываем, когда загрузка завершена
                    webView.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }

        loadMobileArticle(url)
    }

    private fun loadMobileArticle(url: String) {

        val request = Request.Builder()
            .url(url)
            // КЛЮЧ — правильный мобильный User-Agent (именно он заставляет сайт отдать m-версию)
            .header("User-Agent", Constants.USER_AGENT_MOBILE)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .header("Accept-Language", "ru-RU,ru;q=0.9")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@FullActivity, "Нет интернета", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    if (!resp.isSuccessful) {
                        runOnUiThread { Toast.makeText(this@FullActivity, "Ошибка ${resp.code}", Toast.LENGTH_SHORT).show() }
                        return
                    }

                    val html = resp.body.string()
                    val cleanHtml = cleanHtmlForMobile(html)

                    runOnUiThread {
                        webView.loadDataWithBaseURL(
                            Constants.BASE_URL,
                            cleanHtml,
                            "text/html",
                            "UTF-8",
                            null
                        )
                    }
                }
            }
        })
    }

    private fun cleanHtmlForMobile(html: String): String {
        if (html.isBlank()) return "<html><body>Пустая страница</body></html>"

        val doc = try {
            Jsoup.parse(html)
        } catch (e: Exception) {
            return "<html><body>Ошибка обработки страницы</body></html>"
        }



        // Объединяем удаление в один проход (быстрее и читаемее)
        doc.select("""
        script, noscript, iframe, 
        header, footer, nav, aside, 
        .advert, .banner, .reklama, 
        [style*=position:fixed], [class*=ad], [id*=ad], 
        i, p img[src*='plashka-novaya.jpg']""").remove()

        // Удаляем всё после рекламного <p> (включая сам <p>)
        doc.select("p:contains(Читайте новости Краснодара и края в удобном формате в нашем Телеграм-канале и в MAX.)")
            .firstOrNull()
            ?.let { adNode ->
                adNode.nextElementSiblings().forEach { it.remove() }
                adNode.remove()
            }

        // Отключаем все ссылки одним проходом (чисто и эффективно)
        doc.select("a[href]").forEach { link ->
            link.removeAttr("href")
                .removeAttr("target")
                .removeAttr("rel")
                .tagName("span")  // опционально — если хочешь полностью убрать <a>
        }

        // Выбираем контент — более надёжно
        val article = doc.selectFirst("article")
            ?: doc.selectFirst(".news-text, .news-detail, .content, .text, .bloknot-detail-text")
            ?: doc.body()

        // Стили выносим в константу (можно даже в отдельный файл)
        val articleStyle = """
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            body { font-family: 'Roboto', sans-serif; line-height: 1.6; padding: 16px; margin: 0; background: white; color: black; }
            
            img { 
                max-width: 100%; 
                height: auto; 
                border-radius: 12px; 
                margin: 20px 0; 
                box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            }
            
            h1, h2 { color: #1a1a1a; font-size: 14px;}
            p { font-size: 17px; margin: 16px 0; }
            a { color: #0066cc; }
        </style>
    """.trimIndent()

        return buildString {
            append("<!DOCTYPE html><html><head><meta charset=\"utf-8\">")
            append(articleStyle)
            append("</head><body>")
            append(article.html())
            append("</body></html>")
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}