package ru.bloknot.news.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import kotlinx.coroutines.launch
import ru.bloknot.news.R
import ru.bloknot.news.adapters.CustomAdapter
import ru.bloknot.news.databinding.ActivityMainBinding
import ru.bloknot.news.helpclass.RecyclerItemClickListener
import ru.bloknot.news.models.CardNews
import ru.bloknot.news.ui.main.MainUiState
import ru.bloknot.news.ui.main.MainViewModel
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    private lateinit var binding: ActivityMainBinding

    private var currentNews: List<CardNews> = emptyList()
    private var listFullUrls: List<String> = emptyList()
    private var category: String? = null
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

        recyclerView = findViewById(R.id.recycler)
        customAdapter = CustomAdapter(emptyList())
        recyclerView.adapter = customAdapter
        setupRecyclerClickListener()

        category = intent.getStringExtra("cat")
        setTitle(category)
        val pos = intent.getIntExtra("position", 0)
        val urls = intent.getStringArrayListExtra("list_url")

        if (urls == null) {
            Toast.makeText(this, "Ошибка: нет списка URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        observeUiState()
        viewModel.loadNews(urls[pos], category)
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is MainUiState.Loading -> Log.d(TAG, "Loading...")
                    is MainUiState.Success -> {
                        currentNews = state.news
                        listFullUrls = state.urls
                        customAdapter = CustomAdapter(state.news)
                        recyclerView.adapter = customAdapter
                    }
                    is MainUiState.Error -> {
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_SHORT).show()
                        if (state.noInternet) finish()
                    }
                }
            }
        }
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
                if (isDestroyed && mBannerAd != null) mBannerAd?.destroy()
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