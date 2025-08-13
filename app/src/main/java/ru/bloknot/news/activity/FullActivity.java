package ru.bloknot.news.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import java.util.List;

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
        Intent intent = getIntent();
        int count = intent.getIntExtra("count", 0);
        System.out.println("count = " + count);
        list = intent.getStringArrayListExtra("list_url");

        if (list != null){
            String url = list.get(count);
            new JsoupTask(this).execute(url, "a.sys");
        }else {
            System.out.println("Error");
        }

//        new ParseTaskFull(this).execute(count);

    }

    @Override
    public void onPreExecute() {
        System.out.println("JsoupTaskonPreExecute");
    }

    @Override
    public void onPostExecute(Elements result) {
        String url_full_news = "https://bloknot-krasnodar.ru" + result.select("a").attr("href");
        hreadStart(url_full_news, result);
        System.out.println("JsoupTask: " + url_full_news);
    }

    @Override
    public void onError(Exception e) {
        System.out.println("JsoupTask: " + e);
    }

    public void hreadStart(String url, Elements elements){
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
                System.out.println("Получение полной новости: " + res);

//                String img_url = "https:" + doc.select("img").eq(0).attr("src");
//                System.out.println("Ссылка на картинку: " + img_url);

                String cleanedHtml = HtmlCleaner.removeScripts(res);
                list.add(cleanedHtml);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(Html.fromHtml(cleanedHtml, Html.FROM_HTML_OPTION_USE_CSS_COLORS));
                    }
                });
            }
        }).start();
    }

    @SuppressLint("StaticFieldLeak")
    public class ParseTaskFull extends AsyncTask<Integer, Void, String> {
        private final List<String> list = new ArrayList<>();
        private ProgressDialog dialog;
        @SuppressLint("StaticFieldLeak")
        private Context context;
        private String res;
        private Document doc;

        public ParseTaskFull(Context context) {
            this.context = context;
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
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(Integer... count) {

            try {
                doc = Jsoup.connect("https://bloknot-krasnodar.ru/auto/").get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            doc.select("ul.bigline").select("a.sys").forEach(element -> {
                String url_full_news = "https://bloknot-krasnodar.ru/" + element.select("a").attr("href");

                try {

                    doc = Jsoup.connect(url_full_news).get();
                    res = doc.select("div.news-text").html();
                    System.out.println("Получение полной новости: " + res);

                    String string = element.append("img").attr("src");
                    System.out.println("Ссылка на картинку: " + string);

                    String cleanedHtml = HtmlCleaner.removeScripts(res);
                    list.add(cleanedHtml);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            });

            return list.get(count[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println("onPostExecute2: " + result);


            textView.setText(Html.fromHtml(result, Html.FROM_HTML_MODE_LEGACY));

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

        }
    }

}