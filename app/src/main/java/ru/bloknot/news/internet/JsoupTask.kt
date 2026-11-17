package ru.bloknot.news.internet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import ru.bloknot.news.R;
import ru.bloknot.news.interfaces.JsoupParseCallback;
import ru.bloknot.news.utils.Constants;

/** @noinspection deprecation*/ // AsyncTask для парсинга HTML с помощью Jsoup
public class JsoupTask extends AsyncTask<String, Integer, Elements> {

    String BASE_URL = "https://bloknot-krasnodar.ru";

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

        if (url.equals("null")){
            url = Constants.BASE_URL;
        }else {
            url = params[0];
        }

        String cssQuery = params.length > 1 ? params[1] : "body"; // CSS-селектор для выборки элементов

        for (int i = 0; i < 101; i++) {
            publishProgress(i);
        }
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent(context.getResources().getString(R.string.userAgentMobile))
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
