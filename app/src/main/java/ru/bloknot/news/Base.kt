package ru.bloknot.news

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.yandex.mobile.ads.appopenad.AppOpenAd
import com.yandex.mobile.ads.appopenad.AppOpenAdEventListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoadListener
import com.yandex.mobile.ads.appopenad.AppOpenAdLoader
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

open class BaseActivity : AppCompatActivity() {

    val yandexIdAds = "R-M-18977543-1" // для отладки можно использовать "demo-appopenad-yandex"

    protected fun adsYandex() {

        val appOpenAdEventListener = AdEventListener()
        val appOpenAdLoader = AppOpenAdLoader(this)
        val adRequestConfiguration = AdRequestConfiguration.Builder(yandexIdAds).build()

        val appOpenAdLoadListener = object : AppOpenAdLoadListener {
            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                // The ad was loaded successfully. Now you can show loaded ad.
                appOpenAd.show(this@BaseActivity)
                appOpenAd.setAdEventListener(appOpenAdEventListener)
            }

            override fun onAdFailedToLoad(error: AdRequestError) {
                // Ad failed to load with AdRequestError.
                // Attempting to load a new ad from the onAdFailedToLoad() method is strongly discouraged.
            }
        }

        appOpenAdLoader.setAdLoadListener(appOpenAdLoadListener)
        appOpenAdLoader.loadAd(adRequestConfiguration)
    }

    class AdEventListener : AppOpenAdEventListener {
        override fun onAdShown() {
            Log.d("YandexAds", "Показали рекламу успешно!")
        }

        override fun onAdFailedToShow(adError: AdError) {
            Log.d("YandexAds", "Ошибка: $adError")
        }

        override fun onAdDismissed() {
            Log.d("YandexAds", "onAdDismissed")
        }

        override fun onAdClicked() {
            Log.d("YandexAds", "Клик по рекламе")
        }

        override fun onAdImpression(impressionData: ImpressionData?) {
            Log.d("YandexAds", "Что то: $impressionData")
        }
    }
}