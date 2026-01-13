// File: FullActivity.kt
package ru.bloknot.news.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
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
        webView.settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE

        val url = intent.getStringExtra("url") ?: run {
            Toast.makeText(this, "Ошибка: нет ссылки", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadMobileArticle(url)
    }

    private fun loadMobileArticle(url: String) {
        progressBar.visibility = View.VISIBLE

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
                        progressBar.visibility = View.GONE
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

        progressBar.visibility = View.VISIBLE

        val doc = Jsoup.parse(html)

        // Удаляем всё лишнее
        doc.select("script, noscript, iframe, header, footer, nav, aside, .advert, .banner, .reklama").remove()
        doc.select("[style*=position:fixed], [class*=ad], [id*=ad]").remove()
        doc.select("i").remove();
        doc.select("p img[src*='plashka-novaya.jpg']").parents().remove()
        doc.select("i:contains(Читайте новости Краснодара)").remove();

        // Вариант 2: если хочешь оставить тег <a>, но убрать ссылку (чтобы текст остался, но без перехода)
        doc.select("a[href]").forEach { link ->
            link.removeAttr("href")           // убираем атрибут href
            link.tagName("span")              // (опционально) меняем тег <a> на <span>
            link.removeAttr("target")         // убираем target="_blank" если был
            link.removeAttr("rel")            // убираем rel="noopener" и т.п.
        }

        // Оставляем только основной контент
        val article = doc.select("article").first()
            ?: doc.select(".news-text").first()
            ?: doc.body()

        // Убираем лишние отступы и делаем читаемый текст
        val style = """
            <style>
                body { font-family: 'Roboto', sans-serif; line-height: 1.6; padding: 16px; margin: 0; background: white; color: black; }
                img { max-width: 100%; height: auto; border-radius: 8px; margin: 12px 0; }
                h1, h2 { color: #1a1a1a; font-size: 14px;}
                p { font-size: 17px; margin: 16px 0; }
                a { color: #0066cc; }
            </style>
        """.trimIndent()

        return "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'>$style</head><body>${article.html()}</body></html>"
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}