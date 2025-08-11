package ru.bloknot.news.internet;

import org.jsoup.select.Elements;

// Интерфейс обратного вызова для парсинга
public interface JsoupParseCallback {
    void onPreExecute();
    void onPostExecute(Elements result);
    void onError(Exception e);
}
