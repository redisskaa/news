// File: app/src/main/java/ru/bloknot/news/activity/MainActivity.kt
package ru.bloknot.news.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import ru.bloknot.news.R
import ru.bloknot.news.adapters.CustomAdapter
import ru.bloknot.news.databinding.ActivityMainBinding
import ru.bloknot.news.helpclass.NetworkCheck
import ru.bloknot.news.helpclass.RecyclerItemClickListener
import ru.bloknot.news.models.CardNews
import java.io.IOException
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    private var currentNews: List<CardNews> = emptyList()
    private var listFullUrls: ArrayList<String> = arrayListOf()
    private var category: String? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private var mBannerAd: BannerAdView? = null

    private val adUnitId = "R-M-18977543-2"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) { }

        binding.adContainerView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.adContainerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    mBannerAd = loadBannerAd(getAdSize(), adUnitId)
                }
            }
        )

        Log.d(TAG, "Стартуем MainActivity")
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        recyclerView = findViewById(R.id.recycler)
        customAdapter = CustomAdapter(emptyList())
        recyclerView.adapter = customAdapter

        // ✅ Один слушатель на всю Activity — не накапливается
        setupRecyclerClickListener()

        category = intent.getStringExtra("cat")
        setTitle(category)
        val pos = intent.getIntExtra("position", 0)
        val urls = intent.getStringArrayListExtra("list_url")

        Log.d(TAG, "list url: $urls")

        if (urls == null) {
            Toast.makeText(this, "Ошибка: нет списка URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadNews(urls[pos])
    }

    private fun setupRecyclerClickListener() {
        recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                this,
                recyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val url = listFullUrls.getOrNull(position) ?: run {
                            Log.e(TAG, "Нет URL для позиции $position")
                            return
                        }
                        val newsItem = currentNews.getOrNull(position) ?: run {
                            Log.e(TAG, "Нет новости для позиции $position")
                            return
                        }

                        Log.d(TAG, "Открываем статью: $url")
                        val intent = Intent(this@MainActivity, FullActivity::class.java)
                        intent.putExtra("url", url)
                        intent.putExtra("position", position)
                        intent.putExtra("title", newsItem.title)
                        startActivity(intent)
                    }

                    override fun onLongItemClick(view: View, position: Int) { }
                }
            )
        )
    }

    private fun loadNews(url: String) {
        lifecycleScope.launch {
            try {
                // 1. Парсинг URL-ов статей
                listFullUrls = withContext(Dispatchers.IO) { parseCategoryUrls(url) }
                Log.d(TAG, "Найдено URL-ов статей: ${listFullUrls.size}")

                // 2. Проверка сети — в Main-потоке (finish + Toast)
                if (!NetworkCheck.isNetworkConnected(this@MainActivity)) {
                    Toast.makeText(this@MainActivity, "Приложение не работает без интернета", Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }

                // 3. Парсинг новостей
                val news = withContext(Dispatchers.IO) { parseNews(url) }

                // 4. Обновление UI
                if (news.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки, попробуйте позже", Toast.LENGTH_SHORT).show()
                } else {
                    currentNews = news
                    customAdapter = CustomAdapter(news)
                    recyclerView.adapter = customAdapter
                }
            } catch (e: IOException) {
                Log.e(TAG, "Ошибка парсинга: $url", e)
                Toast.makeText(this@MainActivity, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseCategoryUrls(url: String): ArrayList<String> {
        val result = arrayListOf<String>()
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
        } catch (e: IOException) {
            Log.e(TAG, "parseCategoryUrls error: $url", e)
        }
        return result
    }

    private fun parseNews(url: String): List<CardNews> {
        val result = mutableListOf<CardNews>()
        try {
            val doc = Jsoup.connect(url)
                .userAgent(getString(R.string.userAgentMobile))
                .get()

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
        } catch (e: IOException) {
            Log.e(TAG, "parseNews error: $url", e)
        }
        return result
    }

    private fun getAdSize(): BannerAdSize {
        val displayMetrics = resources.displayMetrics
        var adWidthPixels = binding.adContainerView.width
        if (adWidthPixels == 0) {
            adWidthPixels = displayMetrics.widthPixels
        }
        val adWidth = (adWidthPixels / displayMetrics.density).roundToInt()
        return BannerAdSize.stickySize(this, adWidth)
    }

    private fun loadBannerAd(adSize: BannerAdSize, id: String): BannerAdView {
        val bannerAd = binding.adContainerView
        bannerAd.setAdSize(adSize)
        bannerAd.setAdUnitId(id)
        bannerAd.setBannerAdEventListener(object : BannerAdEventListener {
            override fun onAdLoaded() {
                if (isDestroyed && mBannerAd != null) {
                    mBannerAd?.destroy()
                }
            }
            override fun onAdFailedToLoad(error: AdRequestError) { }
            override fun onAdClicked() { }
            override fun onLeftApplication() { }
            override fun onReturnedToApplication() { }
            override fun onImpression(impressionData: ImpressionData?) { }
        })
        val adRequest = AdRequest.Builder().build()
        bannerAd.loadAd(adRequest)
        return bannerAd
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}