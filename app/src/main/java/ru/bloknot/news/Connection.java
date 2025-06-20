package ru.bloknot.news;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Connection {
    String url;
    String query;

    public Connection(String url, String query) {
        this.url = url;
        this.query = query;
    }

    public Connection(String url) {
        this.url = url;
    }

    public void start() {
        new Thread(() -> {
            try {

                Document doc = Jsoup.connect(url).get();

                for (Element element1 : doc.select(query)) {
                    String text = element1.text();
                    System.out.println(text);
                }

            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }).start();
    }
}
