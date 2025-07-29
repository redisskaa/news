package ru.bloknot.news.helpclass;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

            // Дополнительная очистка
            cleanUnsafeAttributes(doc);

            return doc.html();
        } catch (Exception e) {
            e.printStackTrace();
            return html; // Возвращаем исходный HTML при ошибке
        }
    }

    /**
     * Метод для очистки небезопасных атрибутов
     * @param doc Документ Jsoup
     */
    private static void cleanUnsafeAttributes(Document doc) {
        // Перебираем все элементы
        for (Element element : doc.getAllElements()) {
            // Удаляем опасные атрибуты
            element.removeAttr("onclick");
            element.removeAttr("onmouseover");
            element.removeAttr("onmouseout");
            element.removeAttr("onkeypress");
            element.removeAttr("onfocus");
            element.removeAttr("onblur");
        }
    }
}

