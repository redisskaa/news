package ru.bloknot.news;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    List<String> titleList;
    Connection connect;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        connect = new Connection("https://bloknot-krasnodar.ru/", "ul.bigline>li>a.sys");
        connect.start();

    }
}