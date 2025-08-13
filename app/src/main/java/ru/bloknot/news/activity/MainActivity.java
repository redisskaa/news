package ru.bloknot.news.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.bloknot.news.R;
import ru.bloknot.news.adapters.CustomAdapter;
import ru.bloknot.news.helpclass.NetworkCheck;
import ru.bloknot.news.helpclass.RecyclerItemClickListener;
import ru.bloknot.news.internet.ParseTask;
import ru.bloknot.news.models.CardNews;

public class MainActivity extends AppCompatActivity{
    RecyclerView recyclerView;
    CustomAdapter customAdapter;
    List<CardNews> arrayList;
    ArrayList<String> list;
    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler);
        arrayList = new ArrayList<>();
        customAdapter = new CustomAdapter(this, arrayList);
        recyclerView.setAdapter(customAdapter);

        Intent intent = getIntent();
        category =  intent.getStringExtra("cat");
        setTitle("Тематика: " + category);
        int pos = intent.getIntExtra("position", 0);
        list = intent.getStringArrayListExtra("list_url");
        System.out.println("list: " + list);
        System.out.println("data: " + category);
        String url = list.get(pos);

            if (NetworkCheck.isNetworkConnected(this)){
                new ParseTask(this, customAdapter, recyclerView).execute(url);
                recView(this);
            }else {
                finish();
                Toast.makeText(this, "Приложение не работает без интернета", Toast.LENGTH_LONG).show();
            }

    }

    public void recView(Context context){
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView,new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                System.out.println("клик: " + position);

                Intent intent = new Intent(context, FullActivity.class);
                intent.putStringArrayListExtra("list_url", list);
                intent.putExtra("count", position);
                startActivity(intent);
            }
            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
    }
}