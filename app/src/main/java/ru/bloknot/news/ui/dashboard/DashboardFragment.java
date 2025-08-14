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

        ListViewAdapter adapter = new ListViewAdapter(getContext(), data);
        listView.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
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