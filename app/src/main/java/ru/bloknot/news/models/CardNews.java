package ru.bloknot.news.models;

import android.util.Log;

public class CardNews {
    String imageViewUrl;
    String fullUrlLink;
    String category;
    String title;
    String time;
    String description;

    public CardNews(String imageViewUrl, String category, String title, String time, String description) {
        this.imageViewUrl = imageViewUrl;
        this.category = category;
        this.title = title;
        this.time = time;
        this.description = description;
        System.out.println("category: " + category);
        System.out.println("title: " + title);
        System.out.println("time: " + time);
        System.out.println("description: " + description);
        System.out.println("URL картинки: " + imageViewUrl);
        System.out.println("-----------------------------");
    }

    public CardNews(String imageViewUrl, String category, String title, String time, String description, String fullUrlLink) {
        this.fullUrlLink = fullUrlLink;
        this.imageViewUrl = imageViewUrl;
        this.category = category;
        this.title = title;
        this.time = time;
        this.description = description;

    }

    public String getFullUrlLink() {
        return fullUrlLink;
    }

    public void setImageView(String imageView) {
        this.imageViewUrl = imageView;
        System.out.println("URL картинки: " + imageView);
    }

    public void setCategory(String category) {
        this.category = category;
        System.out.println("Категория: " + category);
    }

    public void setTitle(String title) {
        this.title = title;
        System.out.println("Тайтл: " + title);
    }

    public void setTime(String time) {
        this.time = time;
        System.out.println("Время: " + time);
    }

    public void setDescription(String description) {
        this.description = description;
        System.out.println("Описание: " + description);
    }

    public CardNews() {
        Log.i("MY_TAG1", "Конструктор без параметров 21 строка");
    }

    public String getImageUrl() {
        return imageViewUrl;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }
}
