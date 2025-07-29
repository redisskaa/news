package ru.bloknot.news.interfaces;

import java.util.List;

public interface OnTaskCompleted {
    void onTaskCompleted(List<String> result);
    void onTaskCompleted(String result);
}
