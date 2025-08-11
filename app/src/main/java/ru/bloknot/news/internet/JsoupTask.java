package ru.bloknot.news.internet;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/** @noinspection deprecation*/ // AsyncTask для парсинга HTML с помощью Jsoup
public class JsoupTask extends AsyncTask<String, Void, Elements> {

    private final JsoupParseCallback callback;
    private Exception error;

    public JsoupTask(JsoupParseCallback callback) {
        this.callback = callback;
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

        try {
            Document doc = Jsoup.connect(url).get();
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
