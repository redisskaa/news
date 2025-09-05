package ru.bloknot.news.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.jsoup.select.Elements;

import java.util.ArrayList;

import ru.bloknot.news.R;
import ru.bloknot.news.databinding.FragmentHomeBinding;
import ru.bloknot.news.internet.JsoupParseCallback;
import ru.bloknot.news.internet.JsoupTask;

public class HomeFragment extends Fragment implements JsoupParseCallback {


    private FragmentHomeBinding binding;
    private ListView listView;
    private final String BASE_URL = "https://bloknot-krasnodar.ru";
    private ArrayList<String> listUrl;
    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Context context = container.getContext();

        progressBar = root.findViewById(R.id.progBar);
        new JsoupTask(this, context).execute(BASE_URL, "a.link_nav_second");

        return root;
    }

    @Override
    public void onPreExecute() {
        System.out.println("onPreExecute");
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPostExecute(Elements result) {

    }

    /**
     * Метод для удаления элементов из ArrayList по списку индексов.
     *
     * @param arrayList   Список, из которого нужно удалить элементы.
     * @param indices     Массив индексов элементов для удаления.
     */
    public static void deleteElementsByIndices(ArrayList<String> arrayList, int[] indices) {
        // Сортируем индексы по убыванию, чтобы избежать смещения при удалении
        for (int i = 0; i < indices.length; i++) {
            for (int j = i + 1; j < indices.length; j++) {
                if (indices[i] < indices[j]) {
                    int temp = indices[i];
                    indices[i] = indices[j];
                    indices[j] = temp;
                }
            }
        }

        // Удаляем элементы по каждому индексу
        for (int index : indices) {
            if (index >= 0 && index < arrayList.size()) {
                arrayList.remove(index);
            }
        }
    }

    @Override
    public void onProgressUpdate(int percent) {
        progressBar.setProgress(percent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onError(Exception e) {
        System.out.println("onError: " + e);
    }
}