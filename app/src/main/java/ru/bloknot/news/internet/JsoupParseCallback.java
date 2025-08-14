package ru.bloknot.news.internet;

import org.jsoup.select.Elements;


public interface JsoupParseCallback {

    void onProgressUpdate(int percent);
    void onPreExecute();
    void onPostExecute(Elements result);
    void onError(Exception e);
}
