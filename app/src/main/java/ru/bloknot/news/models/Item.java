package ru.bloknot.news.models;

public class Item {
    private String category;

    public Item() {
    }

    public Item(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

