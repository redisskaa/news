package ru.bloknot.news.internet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/** @noinspection deprecation*/ // AsyncTask для парсинга HTML с помощью Jsoup
public class JsoupTask extends AsyncTask<String, Integer, Elements> {

    private final JsoupParseCallback callback;
    private Exception error;
    @SuppressLint("StaticFieldLeak")
    Context context;

    public JsoupTask(JsoupParseCallback callback, Context context) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        callback.onProgressUpdate(values[0]);
    }

    @Override
    protected void onPreExecute() {
        if (callback != null) {
            callback.onPreExecute();
        }
    }

    @Override
    protected Elements doInBackground(String... params) {
        String url = params[0];
        String cssQuery = params.length > 1 ? params[1] : "body"; // CSS-селектор для выборки элементов

        for (int i = 0; i < 101; i++) {
            publishProgress(i);
        }
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 YaBrowser/25.6.0.0 Safari/537.36")
                        .get();
                return doc.select(cssQuery);
            } catch (IOException e) {
                error = e;
                return null;
            }
    }

    @Override
    protected void onPostExecute(Elements elements) {
        if (callback != null) {
            if (elements != null) {
                callback.onPostExecute(elements);
            } else {
                callback.onError(error);
            }
        }
    }
}
