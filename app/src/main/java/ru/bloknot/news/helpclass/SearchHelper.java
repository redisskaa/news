package ru.bloknot.news.helpclass;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.bloknot.news.models.CardNews;

public class SearchHelper {

    public static List<CardNews> search(String query, List<CardNews> allNews) {
        List<CardNews> results = new ArrayList<>();

        if (query == null || query.trim().length() < 3) {
            return results; // меньше 3 букв — ничего не возвращаем
        }

        String lowerQuery = query.trim().toLowerCase(Locale.getDefault());

        for (CardNews news : allNews) {
            String title = news.getTitle() != null ? news.getTitle().toLowerCase(Locale.getDefault()) : "";
            String desc = news.getDescription() != null ? news.getDescription().toLowerCase(Locale.getDefault()) : "";
            String cat = news.getCategory() != null ? news.getCategory().toLowerCase(Locale.getDefault()) : "";

            if (title.contains(lowerQuery) ||
                    desc.contains(lowerQuery) ||
                    cat.startsWith(lowerQuery)) {  // первые буквы категории
                results.add(news);
            }
        }

        return results;
    }
}