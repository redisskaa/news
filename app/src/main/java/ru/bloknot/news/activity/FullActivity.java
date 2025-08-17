package ru.bloknot.news.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import ru.bloknot.news.R;

///* Тебе надо поменять [ссылку](https://m.bloknot-krasnodar.ru/auto/) на [ссылку](https://m.bloknot-krasnodar.ru/news/auto/)
/// И по такому принципу сделать все остальные, а также воможно не придется удалять ссылки
/// в методе ru.bloknot.news.ui.dashboard.DashboardFragment.onPostExecute(), насколько я вижу они нормально отображаются

public class FullActivity extends Activity {

    WebView mWebView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full);

        String url = getIntent().getStringExtra("url");
        int position = getIntent().getIntExtra("position", 0);
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
        settings.setUserAgentString(getResources().getString(R.string.userAgentMobile));
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.loadUrl(url);
    }

    private class MyWebChromeClient extends WebChromeClient {
        public void onProgressChanged(WebView view, int progress) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(progress);

            if (progress == 100) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private static class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            return !isValidDomain(url);
        }

        private boolean isValidDomain(String url) {
            // Замените ваш_домен.ru на нужный вам домен
            return url.contains("bloknot-krasnodar.ru");
        }
    }
}