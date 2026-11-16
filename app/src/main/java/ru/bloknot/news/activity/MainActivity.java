package ru.bloknot.news.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;
import com.yandex.mobile.ads.common.MobileAds;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.bloknot.news.R;
import ru.bloknot.news.adapters.CustomAdapter;
import ru.bloknot.news.databinding.ActivityMainBinding;
import ru.bloknot.news.helpclass.NetworkCheck;
import ru.bloknot.news.helpclass.RecyclerItemClickListener;
import ru.bloknot.news.helpclass.SearchHelper;
import ru.bloknot.news.models.CardNews;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    CustomAdapter customAdapter;
    List<CardNews> arrayList;
    ArrayList<String> list;
    ArrayList<String> list_full_urls;
    String category;
    ActivityMainBinding binding;
    FirebaseAnalytics mFirebaseAnalytics;
    BannerAdView mBannerAd;

    String id = "R-M-18977543-2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MobileAds.initialize(this, () -> {
            // now you can use ads
        });


        binding.adContainerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        binding.adContainerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mBannerAd = loadBannerAd(getAdSize(), id);
                    }
                }
        );

        Log.d("Starting", "Стартуем MainActivity");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        recyclerView = findViewById(R.id.recycler);
        arrayList = new ArrayList<>();
        customAdapter = new CustomAdapter(arrayList);
        recyclerView.setAdapter(customAdapter);

        /// Передаем данные из интента DashboardFragment.java
        Intent intent = getIntent();
        category = intent.getStringExtra("cat");
        setTitle(category);
        int pos = intent.getIntExtra("position", 0);
        list = intent.getStringArrayListExtra("list_url");

        System.out.println("list url: " + list);

        assert list != null;
        String url = list.get(pos);

        getFullNews(url, this);
    }

    @NonNull
    private BannerAdSize getAdSize() {

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        // Calculate the width of the ad, taking into account the padding in the ad container.
        int adWidthPixels = binding.adContainerView.getWidth();
        if (adWidthPixels == 0) {
            // If the ad hasn't been laid out, default to the full screen width
            adWidthPixels = displayMetrics.widthPixels;
        }
        final int adWidth = Math.round(adWidthPixels / displayMetrics.density);

        return BannerAdSize.stickySize(this, adWidth);
    }

    @NonNull
    private BannerAdView loadBannerAd(@NonNull final BannerAdSize adSize, String id) {
        final BannerAdView bannerAd = binding.adContainerView;
        bannerAd.setAdSize(adSize);
        bannerAd.setAdUnitId(id);
        bannerAd.setBannerAdEventListener(new BannerAdEventListener() {
            @Override
            public void onAdLoaded() {
                // If this callback occurs after the activity is destroyed, you
                // must call destroy and return or you may get a memory leak.
                // Note `isDestroyed` is a method on Activity.

                if (isDestroyed() && mBannerAd != null) {
                    mBannerAd.destroy();
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull final AdRequestError adRequestError) {
                // Ad failed to load with AdRequestError.
                // Attempting to load a new ad from the onAdFailedToLoad() method is strongly discouraged.
            }

            @Override
            public void onAdClicked() {
                // Called when a click is recorded for an ad.
            }

            @Override
            public void onLeftApplication() {
                // Called when user is about to leave application (e.g., to go to the browser), as a result of clicking on the ad.
            }

            @Override
            public void onReturnedToApplication() {
                // Called when user returned to application after click.
            }

            @Override
            public void onImpression(@Nullable ImpressionData impressionData) {
                // Called when an impression is recorded for an ad.
            }
        });
        final AdRequest adRequest = new AdRequest.Builder()
                // Methods in the AdRequest.Builder class can be used here to specify individual options settings.
                .build();
        bannerAd.loadAd(adRequest);
        return bannerAd;
    }

    @SuppressLint("ThreadConstraint")
    public void getFullNews(String url_cat, Context context) {
        new Thread(() -> {
            try {
                Document doc = Jsoup.connect(url_cat).get();
                Elements elements = doc.select("ul.bigline>li>.thumbimage>a");

                list_full_urls = new ArrayList<>();

                /// Извлекаем все элементы <a> с атрибутом href
                Elements links = elements.select("a[href]");
                Elements elementsFull = doc.select("a.thumbimage[href]");
                Elements elements1 = doc.select("div.structure-section>a.structure-section__item[href]");
                Elements konkursy = doc.select(".last__item>a[href]");

                /// Если найдены элементы, то выполняем действия дальше
                if (elements.is("a[href]")){
                    // Проходим по всем найденным ссылкам и выводим их значения
                    for (Element link : links) {
                        list_full_urls.add(link.attr("abs:href"));
                    }
                    System.out.println("Выполнили 1 сценарий парсинга");

                }else {
                    for (Element link : elementsFull) {
                        list_full_urls.add(link.attr("abs:href"));
                    }
                    System.out.println("Выполнили 2 сценарий парсинга");
                }

                if (elements1.is("div.structure-section>a.structure-section__item[href]")){
                    for (Element link : elements1) {
                        list_full_urls.add(link.attr("abs:href"));
                    }
                    System.out.println("Выполнили 3 сценарий парсинга");
                }

                if (konkursy.is(".last__item>a[href]")){
                    for (Element link : konkursy) {
                        list_full_urls.add(link.attr("abs:href"));
                    }
                    System.out.println("Выполнили 4 сценарий парсинга");
                }

                if (NetworkCheck.isNetworkConnected(context)) {
                    new ParseTask(context, customAdapter, recyclerView).execute(url_cat);
                } else {
                    finish();
                    Toast.makeText(context, "Приложение не работает без интернета", Toast.LENGTH_LONG).show();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    // Метод фильтрации — использует SearchHelper
    private void filterNews(String query) {
        List<CardNews> filtered = SearchHelper.search(query, arrayList);

        // Обновляем адаптер
        customAdapter.updateNews(new ArrayList<>(filtered));  // передаём отфильтрованный список
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.top_menu, menu);  // твой menu/top_menu.xml
//
//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) searchItem.getActionView();
//
//        assert searchView != null;
//        searchView.setQueryHint("Поиск по новостям...");
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                searchView.clearFocus();  // закрываем клавиатуру
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                List<CardNews> filtered = SearchHelper.search(newText, arrayList);
//                customAdapter.filter(newText, new ArrayList<>(filtered));
//                return true;
//            }
//        });
//
//        // При сворачивании поиска — возвращаем полный список
//        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
//
//                return true;
//            }
//        });
//
//        return true;
//    }

    public void recView(Context context, List<CardNews> listObj) throws IndexOutOfBoundsException{

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                CardNews cardNews = listObj.get(position);

                int posi = listObj.indexOf(cardNews);
                String url = list_full_urls.get(position);
                System.out.println("Передал url: " + url);
                if (position == posi) {
                    String title = cardNews.getTitle();
                    Intent intent = new Intent(context, FullActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("position", posi);
                    intent.putExtra("title", title);
                    startActivity(intent);
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
    }

    @SuppressLint("StaticFieldLeak")
    public class ParseTask extends AsyncTask<String, Void, List<CardNews>> {

        private final Context context;
        private CustomAdapter adapter;
        RecyclerView recyclerView;
        Document doc;
        String cat = "";

        /**
         * @noinspection deprecation
         */
        public ParseTask(Context context, CustomAdapter adapter, RecyclerView recyclerView) {
            this.context = context;
            this.adapter = adapter;
            this.recyclerView = recyclerView;
        }

        @Override
        protected void onPostExecute(List<CardNews> result) {
            super.onPostExecute(result);
            System.out.println(result);
            if (result == null || result.isEmpty()) {
                Toast.makeText(context, "Ошибка загрузки, попробуйте позже", Toast.LENGTH_SHORT).show();
            } else {
                adapter = new CustomAdapter(result);
                recyclerView.setAdapter(adapter);
                recView(context, result);
//                System.out.println("onPostExecute: " + result);
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled(List<CardNews> list) {
            super.onCancelled(list);
        }

        @Override
        protected List<CardNews> doInBackground(String... params) {

            System.out.println("params " + params[0]);

            try {

                doc = Jsoup.connect(params[0]).userAgent(context.getResources().getString(R.string.userAgentMobile)).get();

                doc.select("ul.bigline>li").forEach(element -> {
                    String title = element.select("a.sys").text();
                    String cat = element.select("a.cat").text();
                    String time = element.select("span.botinfo").text();
                    String description = element.getElementsByTag("p").text();
                    String url_image = "https:" + element.select("img").attr("src");
                    //String linkFull = Objects.requireNonNull(element.select("a.sys").first()).absUrl("href");

                    Element linkElement = element.selectFirst("a.sys");  // selectFirst — удобнее, чем select().first()
                    String linkFull = (linkElement != null) ? linkElement.absUrl("href") : "";

                    System.out.println("params1 " + linkFull);
                    arrayList.add(new CardNews(url_image, cat, title, time, description, linkFull));
                });

                String string = arrayList.toString();

                if (string.equals("[]")) {

                    doc.select("div.catitem-row>div.catitem").forEach(element -> {

                        cat = doc.select("div.news-section-header>h1").text();
                        String title = element.select("a.linksys").text();
                        String time = element.select("span.botinfo").text();
                        String description = element.select("span.previewtext").text();
                        String url_image = "https:" + element.select("img").attr("src");

                        arrayList.add(new CardNews(url_image, category, title, time, description));
                    });

                    doc.select(".last__item>a").forEach(element -> {
                        String time = "в разработке";
                        String title = element.getElementsByClass("last__description").select("h3").text();
                        String url_image = "https:" + element.select("img").attr("src");
                        arrayList.add(new CardNews(url_image, category, title, time, time));
                    });

                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return arrayList;
        }
    }
}