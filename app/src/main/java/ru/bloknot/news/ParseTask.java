package ru.bloknot.news;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.bloknot.news.adapters.CustomAdapter;
import ru.bloknot.news.models.CardNews;

/** @noinspection deprecation*/
@SuppressLint("StaticFieldLeak")
public class ParseTask extends AsyncTask<String, Void, List<CardNews>> {
    private final Context context;
    private CustomAdapter adapter;
    RecyclerView recyclerView;
    private ProgressDialog dialog;
    Document doc;

    public ParseTask (Context context, CustomAdapter adapter, RecyclerView recyclerView) {
        this.context = context;
        this.adapter = adapter;
        this.recyclerView = recyclerView;
    }

    public ParseTask (Context context, CustomAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage("Загрузка данных...");
        dialog.show();
    }

    @Override
    protected List<CardNews> doInBackground(String... params) {

        List<CardNews> contentList = new ArrayList<>();

        try {
            doc = Jsoup.connect("https://bloknot-krasnodar.ru/").get();

            doc.select("ul.bigline>li").forEach(element -> {
                String title = element.select("a.sys").text();
                String category = element.select("a.cat").text();
                String time = element.select("span.botinfo").text();
                String description = element.getElementsByTag("p").text();
                String url_image = "https:" + element.select("img").attr("src");

//                System.out.println("title: " + title);
//                System.out.println("category: " + category);
//                System.out.println("description: " + description);
//                System.out.println("url_image: " + url_image);
//                System.out.println("time: " + time);

                contentList.add(new CardNews(url_image, category, title, time, description));

            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentList;
    }

    @Override
    protected void onPostExecute(List<CardNews> result) {
        super.onPostExecute(result);

        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        adapter = new CustomAdapter(context, result);
        recyclerView.setAdapter(adapter);

        System.out.println("onPostExecute: " + result);

    }
}
