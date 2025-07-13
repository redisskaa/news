package ru.bloknot.news;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler);
        if (NetworkCheck.isNetworkConnected(this)){
            new ParseTask(this, recyclerView).execute();
        }else {
            Toast.makeText(this, "Приложение не работает без интернета", Toast.LENGTH_LONG).show();
        }

    }
}