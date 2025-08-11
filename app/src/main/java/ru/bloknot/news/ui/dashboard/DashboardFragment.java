package ru.bloknot.news.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import ru.bloknot.news.R;
import ru.bloknot.news.activity.MainActivity;
import ru.bloknot.news.adapters.ListViewAdapter;
import ru.bloknot.news.databinding.FragmentDashboardBinding;
import ru.bloknot.news.internet.JsoupParseCallback;
import ru.bloknot.news.internet.JsoupTask;
import ru.bloknot.news.models.Item;

public class DashboardFragment extends Fragment implements JsoupParseCallback {

    private FragmentDashboardBinding binding;
    ListView listView;
    List<Item> data = new ArrayList<>();
    private final String BASE_URL = "https://bloknot-krasnodar.ru";
    View root;
    ArrayList<String> listUrl;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        listView = root.findViewById(R.id.myListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = (Item) parent.getItemAtPosition(position);
                System.out.println("item: " + item);
                Intent intent = new Intent(view.getContext(), MainActivity.class);

                switch (position) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 18:
                    case 19:
                    case 20:
                        intent.putExtra("cat", item.getCategory());
                        intent.putStringArrayListExtra("list_url", listUrl);
                        startActivity(intent);
                        break;
                    default:
                        System.out.println("Error");
                        break;
                }
            }
        });

        new JsoupTask(this).execute(BASE_URL, "a.link_nav_second");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPreExecute() {
        System.out.println("onPreExecute");
    }

    @Override
    public void onPostExecute(Elements result) {
        StringBuilder sb = new StringBuilder();

        ListViewAdapter adapter = new ListViewAdapter(getContext(), data);
        listView.setAdapter(adapter);

        for (int i = 0; i < result.size(); i++) {
            sb.append(result.get(i).text()).append("\n");
            data.add(new Item(result.get(i).text()));
        }

        listUrl = new ArrayList<>();

        for (int i = 0; i < result.size(); i++) {
            System.out.println(result.get(i).attr("href"));
            String url = BASE_URL + result.get(i).attr("href");
            listUrl.add(url);
        }

        System.out.println("this is ListUrls: " + listUrl);
    }

    @Override
    public void onError(Exception e) {
        System.out.println("onError: " + e);
    }
}