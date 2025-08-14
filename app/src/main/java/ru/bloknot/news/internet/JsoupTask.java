package ru.bloknot.news.internet;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import ru.bloknot.news.R;

/** @noinspection deprecation*/ // AsyncTask для парсинга HTML с помощью Jsoup
public class JsoupTask extends AsyncTask<String, Integer, Elements> {

    private final JsoupParseCallback callback;
    private Exception error;
    ProgressDialog dialog;
    @SuppressLint("StaticFieldLeak")
    Context context;


    public JsoupTask(JsoupParseCallback callback, Context context) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPreExecute() {
        if (callback != null) {
            dialog = new ProgressDialog(context);
            callback.onPreExecute();
        }
    }

    @Override
    protected Elements doInBackground(String... params) {
        String url = params[0];
        String cssQuery = params.length > 1 ? params[1] : "body"; // CSS-селектор для выборки элементов

        for (int i = 0; i <= 100; i++) {
            publishProgress(i);
            try {
                Document doc = Jsoup.connect(url).userAgent(context.getResources().getString(R.string.userAgent)).get();
                return doc.select(cssQuery);
            } catch (IOException e) {
                error = e;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Elements elements) {
        if (callback != null) {
            if (elements != null) {
                dialog.dismiss();
                callback.onPostExecute(elements);
            } else {
                callback.onError(error);
            }
        }
    }
}
