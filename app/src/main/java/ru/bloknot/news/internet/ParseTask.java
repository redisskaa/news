package ru.bloknot.news.internet;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.bloknot.news.R;
import ru.bloknot.news.adapters.CustomAdapter;
import ru.bloknot.news.models.CardNews;

/**
 * @noinspection deprecation
 */
@SuppressLint("StaticFieldLeak")
public class ParseTask extends AsyncTask<String, Void, List<CardNews>> {
    private final Context context;
    private CustomAdapter adapter;
    RecyclerView recyclerView;
    private ProgressDialog dialog;
    Document doc;
    String cat = "";

    public ParseTask(Context context, CustomAdapter adapter, RecyclerView recyclerView) {
        this.context = context;
        this.adapter = adapter;
        this.recyclerView = recyclerView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<CardNews> doInBackground(String... params) {

        List<CardNews> contentList = new ArrayList<>();
        System.out.println("params " + params[0]);

        try {
            doc = Jsoup.connect(params[0]).userAgent(context.getResources().getString(R.string.userAgent)).get();

            doc.select("ul.bigline>li").forEach(element -> {
                String title = element.select("a.sys").text();
                String cat = element.select("a.cat").text();
                String time = element.select("span.botinfo").text();
                String description = element.getElementsByTag("p").text();
                String url_image = "https:" + element.select("img").attr("src");
                contentList.add(new CardNews(url_image, cat, title, time, description));
            });

            String string = contentList.toString();

            if (string.equals("[]")){

                doc.select("div.catitem-row>div.catitem").forEach(element -> {

                    doc.select("div.news-section-header>h1").forEach(element1 -> {
                        cat = element1.text();
                    });

                    String title = element.select("a.linksys").text();

                    String time = element.select("span.botinfo").text();
                    String description = element.select("span.previewtext").text();
                    String url_image = "https:" + element.select("img").attr("src");

                    contentList.add(new CardNews(url_image, cat, title, time, description));
                });

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentList;
    }

    @Override
    protected void onPostExecute(List<CardNews> result) {
        super.onPostExecute(result);

        if (result == null || result.isEmpty()) {
            Toast.makeText(context, "Ошибка загрузки, попробуйте позже", Toast.LENGTH_SHORT).show();
        } else {
            adapter = new CustomAdapter(context, result);
            recyclerView.setAdapter(adapter);
            System.out.println("onPostExecute: " + result);
        }

    }
}
