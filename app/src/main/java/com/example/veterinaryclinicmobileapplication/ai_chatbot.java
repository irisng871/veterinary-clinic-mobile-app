package com.example.veterinaryclinicmobileapplication;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ai_chatbot extends AppCompatActivity {

    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_chatbot);

        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> goBackHomePage());

        WebView landbotWebView = findViewById(R.id.landbotWebView);
        WebSettings webSettings = landbotWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        landbotWebView.setWebViewClient(new WebViewClient());
        landbotWebView.loadUrl("https://landbot.online/v3/H-2639930-UNS9M53RRTJFLK9Z");
    }

    public void goBackHomePage() {
        finish();
    }
}
