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
    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler);
        arrayList = new ArrayList<>();
        customAdapter = new CustomAdapter(this, arrayList);
        recyclerView.setAdapter(customAdapter);

        Intent intent = getIntent();
        category = intent.getStringExtra("cat");
        setTitle("Тема >> " + category);
        int pos = intent.getIntExtra("position", 0);

        list = intent.getStringArrayListExtra("list_url");

        assert list != null;
        String url = list.get(pos);

        if (NetworkCheck.isNetworkConnected(this)) {
            new ParseTask(this, customAdapter, recyclerView).execute(url);
        } else {
            finish();
            Toast.makeText(this, "Приложение не работает без интернета", Toast.LENGTH_LONG).show();
        }

    }

    public void recView(Context context, List<CardNews> listObj) {

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                CardNews cardNews = listObj.get(position);
                int posi = listObj.indexOf(cardNews);

                System.out.println("position: " + position);
                System.out.println("posi: " + posi);
                String url = list.get(position);
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

            if (result == null || result.isEmpty()) {
                Toast.makeText(context, "Ошибка загрузки, попробуйте позже", Toast.LENGTH_SHORT).show();
            } else {
                adapter = new CustomAdapter(context, result);
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

                doc = Jsoup.connect(params[0]).userAgent(context.getResources().getString(R.string.userAgent)).get();

                doc.select("ul.bigline>li").forEach(element -> {
                    String title = element.select("a.sys").text();
                    String cat = element.select("a.cat").text();
                    String time = element.select("span.botinfo").text();
                    String description = element.getElementsByTag("p").text();
                    String url_image = "https:" + element.select("img").attr("src");
                    arrayList.add(new CardNews(url_image, cat, title, time, description));
                });

                String string = arrayList.toString();

                if (string.equals("[]")) {

                    doc.select("div.catitem-row>div.catitem").forEach(element -> {
                        cat = doc.select("div.news-section-header>h1").text();
                        String title = element.select("a.linksys").text();
                        String time = element.select("span.botinfo").text();
                        String description = element.select("span.previewtext").text();
                        String url_image = "https:" + element.select("img").attr("src");

                        arrayList.add(new CardNews(url_image, cat, title, time, description));
                    });

                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return arrayList;
        }
    }
}