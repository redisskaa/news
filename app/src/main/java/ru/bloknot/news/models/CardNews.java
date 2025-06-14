package ru.bloknot.news.models;

import android.util.Log;
import android.widget.ImageView;

public class CardNews {
    ImageView imageView;
    String category;
    String title;
    String time;
    String description;
    public CardNews(ImageView imageView, String category, String title, String time, String description) {
        this.imageView = imageView;
        this.category = category;
        this.title = title;
        this.time = time;
        this.description = description;
        Log.i("MY_TAG1", "Конструктор с параметрами");
    }

    public CardNews() {
        Log.i("MY_TAG1", "Конструктор без параметров 21 строка");
    }

    public ImageView getImageView() {
        return imageView;
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
