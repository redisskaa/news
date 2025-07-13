package ru.bloknot.news;

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

public class ParseTask extends AsyncTask<Void, Void, List<CardNews>> {
    private final Context context;
    private RecyclerView recyclerView;
    private ProgressDialog dialog;

    public ParseTask (Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Показываем прогресс-бар
        dialog = new ProgressDialog(context);
        dialog.setMessage("Загрузка данных...");
        dialog.show();
    }

    @Override
    protected List<CardNews> doInBackground(Void... params) {

        List<CardNews> contentList = new ArrayList<>();

        try {
            Document doc = Jsoup.connect("https://bloknot-krasnodar.ru/")
                    .timeout(5000)
                    .get();

            doc.select("ul.bigline>li").forEach(element -> {
                String title = element.select("a.sys").text();
                String category = element.select("a.cat").text();
                String time = element.select("span.botinfo").text();
                String description = element.getElementsByTag("p").text();
                String url_image = "https:" + element.select("img").attr("src");

                contentList.add(new CardNews(url_image, category, title, time, description));

                System.out.println("title: " + title);
                System.out.println("category: " + category);
                System.out.println("description: " + description);
                System.out.println("url_image: " + url_image);
                System.out.println("time: " + time);
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

        CustomAdapter adapter = new CustomAdapter(context, result);
        recyclerView.setAdapter(adapter);
    }
}
