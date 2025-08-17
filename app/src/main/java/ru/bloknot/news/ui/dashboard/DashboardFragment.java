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

import ru.bloknot.news.R;
import ru.bloknot.news.activity.MainActivity;
import ru.bloknot.news.adapters.ListViewAdapter;
import ru.bloknot.news.databinding.FragmentDashboardBinding;
import ru.bloknot.news.internet.JsoupParseCallback;
import ru.bloknot.news.internet.JsoupTask;

public class DashboardFragment extends Fragment implements JsoupParseCallback {

    private FragmentDashboardBinding binding;
    private ListView listView;
    private final String BASE_URL = "https://bloknot-krasnodar.ru";
    private ArrayList<String> listUrl;
    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Context context = container.getContext();
        listView = root.findViewById(R.id.myListView);
        progressBar = root.findViewById(R.id.progBar);
        new JsoupTask(this, context).execute(BASE_URL, "a.link_nav_second");
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String itemValue = parent.getItemAtPosition(position).toString();
            String pos = listUrl.get(position);
            int posi = listUrl.indexOf(pos);

            if (position == posi) {
                Intent intent = new Intent(root.getContext(), MainActivity.class);
                intent.putExtra("cat", itemValue);
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

        StringBuilder sb = new StringBuilder();
        ArrayList<String> data = new ArrayList<>();

        for (int b = 0; b < result.size(); b++) {
            sb.append(result.get(b).text()).append("\n");
            data.add(result.get(b).text());
        }

        listUrl = new ArrayList<>();

        for (int c = 0; c < result.size(); c++) {
            String url = BASE_URL + result.get(c).attr("href");
            listUrl.add(url);
        }

//        System.out.println("\nСписок до удаления");
//        System.out.println(data);
//        System.out.println("\nСписок до удаления");
//        System.out.println(listUrl);
//        System.out.println("-----------------------");

        int[] indicesToRemove = {2, 4, 7, 6, 15, 18};

        deleteElementsByIndices(data, indicesToRemove);
        deleteElementsByIndices(listUrl, indicesToRemove);

//        System.out.println("\nСписок после удаления:");
//        printArray(data);
//        printArray(listUrl);

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

//    /**
//     * Вспомогательный метод для вывода содержимого списка.
//     *
//     * @param arrayList Список для печати.
//     */
//    public static void printArray(ArrayList<String> arrayList) {
//        for (String element : arrayList) {
//            System.out.println(element);
//        }
//    }

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