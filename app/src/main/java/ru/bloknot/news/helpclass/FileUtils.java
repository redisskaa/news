package ru.bloknot.news.helpclass;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileUtils<T> {
    private static final String FILENAME = "data.txt";
    private static final String TAG = "FileUtils";

    // Метод для сохранения текста в файл
    public void saveData (Context context, T savedata) {
        try (FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
             OutputStreamWriter osw = new OutputStreamWriter(fos)) {
            osw.write(String.valueOf(savedata));
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при сохранении файла", e);
        }
    }

    public void saveDataList (Context context, ArrayList<String> savedata) {
        try (FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
             OutputStreamWriter osw = new OutputStreamWriter(fos)) {
            osw.write(String.valueOf(savedata));
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при сохранении файла", e);
        }
    }

    // Метод для чтения текста из файла
    public static String loadText(Context context) {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = context.openFileInput(FILENAME);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append('\n');
            }
            return content.toString();
        } catch (FileNotFoundException e) {
            // Файл не найден - это нормально при первом запуске
            return "";
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при чтении файла", e);
            return "";
        }
    }

    // Метод для проверки существования файла
    public static boolean fileExists(Context context) {
        try {
            context.openFileInput(FILENAME);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    // Метод для удаления файла
    public static boolean deleteFile(Context context) {
        try {
            context.deleteFile(FILENAME);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при удалении файла", e);
            return false;
        }
    }
}
