package ru.bloknot.news.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ru.bloknot.news.R;
import ru.bloknot.news.activity.MainActivity;
import ru.bloknot.news.adapters.ListViewAdapter;
import ru.bloknot.news.databinding.FragmentDashboardBinding;
import ru.bloknot.news.interfaces.JsoupParseCallback;
import ru.bloknot.news.internet.JsoupTask;
import ru.bloknot.news.utils.Constants;

public class DashboardFragment extends Fragment implements JsoupParseCallback {

    private FragmentDashboardBinding binding;
    private ListView listView;
    private ArrayList<String> listUrl;
    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Context context = container.getContext();
        listView = root.findViewById(R.id.myListView);
        progressBar = root.findViewById(R.id.progBar);
        new JsoupTask(this, context).execute(Constants.BASE_URL, "a.link_nav_second");
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String itemValue = parent.getItemAtPosition(position).toString();
            String pos = listUrl.get(position);
            int posi = listUrl.indexOf(pos);

            if (position == posi) {
                Intent intent = new Intent(root.getContext(), MainActivity.class);
                intent.putExtra("cat", itemValue);
                intent.putExtra("BASE_URL", Constants.BASE_URL);
                intent.putStringArrayListExtra("list_url", listUrl);
                intent.putExtra("position", posi);
                startActivity(intent);
                Log.d("ListViewClick", "Position: " + posi + ", ID: " + id + ", Value: " + itemValue);
            } else {
                System.out.println("Error");
            }
        });

        return root;
    }

    @Override
    public void onPreExecute() {
        System.out.println("onPreExecute");
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPostExecute(Elements result) {

        ArrayList<String> data = new ArrayList<>();

        try {

            for (int b = 0; b < result.size(); b++) {

                if (b == 0){
                    System.out.println("Первый for: начало");
                    System.out.println("-------------------");
                }

                data.add(result.get(b).text());
                //System.out.println("Список: " + b + " " + data.get(b));
            }

            if (data.size() == result.size()){
                System.out.println("Первый for: конец");
            }

            listUrl = new ArrayList<>();

            for (int c = 0; c < result.size(); c++) {

                if (c == 0){
                    System.out.println("Второй for: начало");
                    System.out.println("-------------------");
                }

                String url = result.get(c).attr("abs:href");
                listUrl.add(url);
            }

            if (listUrl.size() == result.size()){
                System.out.println("Второй for: конец");
            }

            listUrl.set(10, "https://bloknot-krasnodar.ru/news/a_request_to_the_editor/");
            listUrl.set(7, "https://bloknot-krasnodar.ru/news/officials_of_the_city/");

            listUrl.remove(2);
            listUrl.remove(14);
            listUrl.remove(16);
            listUrl.remove(3);

            WordRemover.removeItemsContainingWords(data, getString(R.string.wordsRemove));

        }catch (IndexOutOfBoundsException e){
            System.out.println("Ошибка: " + e.getMessage());
        }


        ListViewAdapter adapter = new ListViewAdapter(getContext(), data);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
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

    public static class WordRemover {

        /**
         * Удаляет из списка элементы, содержащие любое из указанных слов (без учёта регистра).
         * Слова задаются через запятую.
         *
         * @param list          Список строк для обработки
         * @param wordsToRemove Слова для поиска (через запятую), например "кот, собака, птица"
         */
        public static void removeItemsContainingWords(
                ArrayList<String> list,
                String wordsToRemove
        ) {
            // 1. Обрабатываем строку слов: разбиваем, чистим, собираем в набор
            Set<String> wordSet = parseWords(wordsToRemove);

            // 2. Если слов нет — ничего не делаем
            if (wordSet.isEmpty()) {
                return;
            }

            // 3. Удаляем элементы, содержащие хотя бы одно из слов
            list.removeIf(item -> containsAnyWord(item, wordSet));
        }

        // Вспомогательный метод: преобразует строку слов в набор (в нижнем регистре)
        private static Set<String> parseWords(String wordsStr) {
            Set<String> wordSet = new HashSet<>();

            if (wordsStr == null || wordsStr.trim().isEmpty()) {
                return wordSet;
            }

            String[] words = wordsStr.split(",");
            for (String word : words) {
                String trimmedWord = word.trim();
                if (!trimmedWord.isEmpty()) {
                    wordSet.add(trimmedWord.toLowerCase());
                }
            }

            return wordSet;
        }

        // Вспомогательный метод: проверяет, содержит ли строка хотя бы одно слово из набора
        private static boolean containsAnyWord(String item, Set<String> words) {
            String itemLower = item.toLowerCase();

            for (String word : words) {
                if (itemLower.contains(word)) {
                    return true;
                }
            }

            return false;
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