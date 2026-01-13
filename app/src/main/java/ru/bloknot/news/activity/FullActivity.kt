package ru.bloknot.news.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.bloknot.news.R;

///* Открой другой проект app и тебе надо синхронизировать данные с этим классом.
/// А также надо найти способ передавать полные ссылки новостей

public class FullActivity extends Activity {

    WebView mWebView;
    private ProgressBar progressBar;
    TextView textView;
    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full);
        textView = findViewById(R.id.textLoad);
        layout = findViewById(R.id.linerNew);
        String url = getIntent().getStringExtra("url");
        System.out.println("Принял url: " + url);

        if (url != null && !url.isEmpty()) {
            setupWebView(url);
            System.out.println("Не нулл url продолжаем работу");
        }else {
            Toast.makeText(this, "Ошибка загрузки данных!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(String url) {
        mWebView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setUserAgentString(getResources().getString(R.string.user_agent));
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient());
        downloadAndProcessHTML(url);
    }

    private class MyWebChromeClient extends WebChromeClient {
        public void onProgressChanged(WebView view, int progress) {

            String prog = "Загрузка " + progress + " %";
            textView.setText(prog);

            progressBar.setProgress(progress);

            if (progress == 100) {
                mWebView.setVisibility(View.VISIBLE);
                layout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            } else {
                layout.setVisibility(View.VISIBLE);
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();

            if (url.contains("m.bloknot-krasnodar.ru") || url.contains("bloknot-krasnodar.ru")) {
                view.loadUrl(url);
                return true;
            }else {
                System.out.println("Ошибка!");
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
            return true;
        }
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            view.getSettings().setJavaScriptEnabled(false);
            super.onPageStarted(view, url, favicon);
        }
    }

    /// Новый код

    private void downloadAndProcessHTML(String urlSite) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .addHeader("User-Agent", getResources().getString(R.string.user_agent))
                .url(urlSite)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                System.out.println("responce: " + response);

                if (response.code() == 404 || response.message().equals("Not Found")){
                    System.out.println("Ошибка 404");
                }

                if (!response.isSuccessful()) throw new IOException("Ошибка сервера");


                String htmlContent = response.body().string();
                modifyAndLoadHTML(htmlContent);
            }
        });
    }

    private void modifyAndLoadHTML(String htmlContent) {
        Document document = Jsoup.parse(htmlContent);

        /// Удаляем все ссылки
        document.select("a[href]").remove();

        // Удаляем меню верхнее полностью
        Element header = document.getElementById("header");

        if (header != null) {
            header.remove();
        }else {
            System.out.println("header: Не получилось удалить");
        }

        // Удаляем блоки опросов и другие ненужные секции
        Element poll = document.getElementById("pollFrame");

        if (poll != null) {
            poll.remove();
        }else {
            System.out.println("poll: Не получилось удалить");
        }

        document.getElementsByClass("flex.justify-between.bg-white.p-4.mb-2.border-b").remove();
        document.getElementsByTag("section").remove();
        document.getElementsByTag("h1").remove();
        document.getElementsByTag("footer").remove();
        document.getElementsByClass("text_below_content").remove();
        document.getElementsByTag("i").remove();
        document.getElementsByTag("iframe").remove();

        // Меняем стили
        document.head().append("<style> .pt-44{ padding-top:0.0rem; } </style>");
        Element main = document.select("main").first();
        if (main != null){
            main.removeAttr("style");
        }

///        System.out.println("----------------------");
///        System.out.println("main: " + main);
///       System.out.println("----------------------");

        final String modifiedHtml = document.html();

        runOnUiThread(() -> {
            mWebView.loadDataWithBaseURL("https://bloknot-krasnodar.ru/", modifiedHtml, "text/html", "utf-8", null);
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}