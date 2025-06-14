package ru.bloknot.news;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class Connection {
    String url;

    public Connection(String url) {
        this.url = url;
    }

    public void start() {
        new Thread(() -> {
            try {

                Document doc = Jsoup.connect(url).get();

                for (Element element1 : doc.select("ul.bigline>li>a.sys")) {
                    String text = element1.text();
                    System.out.println(text);
                }

            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }).start();
    }
}
