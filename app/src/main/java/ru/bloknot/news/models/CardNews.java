package ru.bloknot.news.models;

import android.util.Log;

public class CardNews {
    String imageViewUrl;
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
        Log.i("MY_TAG1", "Конструктор с параметрами");
    }

    public void setImageView(String imageView) {
        this.imageViewUrl = imageView;
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
    }

    public CardNews(String title, String category, String time) {
        this.title = title;
        this.category = category;
        this.time = time;
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
