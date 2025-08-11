package ru.bloknot.news.helpclass;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlCleaner {
    /**
     * Метод для очистки HTML от скриптов
     * @param html Исходный HTML-код
     * @return Очищенный HTML-код без скриптов
     */
    public static String removeScripts(String html) {
        // Проверка на null
        if (html == null || html.isEmpty()) {
            return "";
        }

        try {
            // Парсинг HTML
            Document doc = Jsoup.parse(html);

            // Удаление всех скриптов
            doc.select("script").remove();
            doc.select("img").remove();

            return doc.html();
        } catch (Exception e) {
            System.out.println("Ошибка HtmlCleaner: " + e);
            return html;
        }
    }
}

