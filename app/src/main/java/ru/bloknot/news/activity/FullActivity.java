package ru.bloknot.news.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import ru.bloknot.news.R;
import ru.bloknot.news.helpclass.HtmlCleaner;
import ru.bloknot.news.internet.JsoupParseCallback;
import ru.bloknot.news.internet.JsoupTask;

public class FullActivity extends AppCompatActivity implements JsoupParseCallback {
    private TextView textView;
    ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full);

        /// Виджеты
        textView = findViewById(R.id.fullText);
        textView.setMovementMethod(new ScrollingMovementMethod());
        startMethod();
    }

    public void startMethod() {
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        System.out.println("url = " + url);
        list = intent.getStringArrayListExtra("list_url");
        new JsoupTask(this, this).execute(url, "a.sys");
    }

    @Override
    public void onProgressUpdate(int percent) {
//        runOnUiThread(() -> {
//            System.out.println("Загрузка " + percent + " %");
//        });
    }

    @Override
    public void onPreExecute() {
        System.out.println("JsoupTaskonPreExecute");
    }

    @Override
    public void onPostExecute(Elements result) {
        String url_full_news = "https://bloknot-krasnodar.ru" + result.select("a").attr("href");
        hreadStart(url_full_news);
        System.out.println("JsoupTask: " + url_full_news);
    }

    @Override
    public void onError(Exception e) {
        System.out.println("JsoupTask: " + e);
    }

    public void hreadStart(String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Document doc;
                try {
                    doc = Jsoup.connect(url).get();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String res = doc.select("div.news-text").html();
                String res1 = doc.select("div.news-text").text();
                System.out.println("Получение полной новости: " + res1);

//                String img_url = "https:" + doc.select("img").eq(0).attr("src");
//                System.out.println("Ссылка на картинку: " + img_url);

                String cleanedHtml = HtmlCleaner.removeScripts(res);

                runOnUiThread(() -> textView.setText(Html.fromHtml(cleanedHtml, Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH)));
            }
        }).start();
    }

}