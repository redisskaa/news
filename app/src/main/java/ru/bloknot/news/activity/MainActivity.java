package ru.bloknot.news.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.bloknot.news.ParseTask;
import ru.bloknot.news.R;
import ru.bloknot.news.adapters.CustomAdapter;
import ru.bloknot.news.helpclass.NetworkCheck;
import ru.bloknot.news.models.CardNews;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    CustomAdapter customAdapter;
   List<CardNews> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler);
        arrayList = new ArrayList<>();
        customAdapter = new CustomAdapter(this, arrayList);
        recyclerView.setAdapter(customAdapter);

//        String savedText = FileUtils.loadText(this);
//        System.out.println("savedText: " + savedText);

        if (NetworkCheck.isNetworkConnected(this)){
            new ParseTask(this, customAdapter, recyclerView).execute();
            }else {
            finish();
            Toast.makeText(this, "Приложение не работает без интернета", Toast.LENGTH_LONG).show();
        }

    }
}