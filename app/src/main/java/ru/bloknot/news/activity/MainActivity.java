package ru.bloknot.news.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.bloknot.news.R;
import ru.bloknot.news.adapters.CustomAdapter;
import ru.bloknot.news.helpclass.NetworkCheck;
import ru.bloknot.news.helpclass.RecyclerItemClickListener;
import ru.bloknot.news.models.CardNews;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    CustomAdapter customAdapter;
    List<CardNews> arrayList;
    ArrayList<String> list;
    ArrayList<String> list_full_urls;
    String category;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

                /// Если найдены элементы то выполняем действия дальше
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

    public void recView(Context context, List<CardNews> listObj) throws IndexOutOfBoundsException{

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                CardNews cardNews = listObj.get(position);
                int posi = listObj.indexOf(cardNews);
                String url = list_full_urls.get(position);
                System.out.println("Передал url: " + url);
                if (position == posi) {
                    Intent intent = new Intent(context, FullActivity.class);
                    intent.putExtra("url", url);
                    intent.putExtra("position", posi);
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