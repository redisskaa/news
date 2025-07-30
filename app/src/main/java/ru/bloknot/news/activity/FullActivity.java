package ru.bloknot.news.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.bloknot.news.R;
import ru.bloknot.news.helpclass.HtmlCleaner;

public class FullActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full);

        /// Виджеты
        textView = findViewById(R.id.fullText);
        textView.setMovementMethod(new ScrollingMovementMethod());

        int count = getIntent().getIntExtra("count", 0);
        System.out.println("count =" + count);
        new ParseTaskFull(this).execute(count);
    }


    @SuppressLint("StaticFieldLeak")
    public class ParseTaskFull extends AsyncTask<Integer, Void, List<String>> {
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
        protected List<String> doInBackground(Integer... count) {

            try {
                doc = Jsoup.connect("https://bloknot-krasnodar.ru/").timeout(2000).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            doc.select("ul.bigline").select("a.sys").forEach(element -> {
                String url_full_news = "https://bloknot-krasnodar.ru" + element.select("a").attr("href");

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

            String s = list.get(count[0]);
            List<String> stringList = new ArrayList<>();
            stringList.add(s);

            return stringList;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            System.out.println("onPostExecute2: " + result);
            String res = result.get(0);

            textView.setText(Html.fromHtml(res, Html.FROM_HTML_MODE_LEGACY));

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

        }
    }

}