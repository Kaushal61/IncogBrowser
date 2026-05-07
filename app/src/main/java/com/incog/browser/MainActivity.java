package com.incog.browser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlBar;
    private Button incognitoToggle;
    private Button desktopToggle;
    
    private boolean isIncognito = false;
    private boolean isDesktop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // IDs ko link kiya hai - NOTE: Apne activity_main.xml me IDs check kar lena
        webView = findViewById(R.id.webview);
        urlBar = findViewById(R.id.url_bar);
        incognitoToggle = findViewById(R.id.btn_incognito);
        desktopToggle = findViewById(R.id.btn_desktop);

        setupWebView();

        // Smart Search Logic (URL vs Google Search)
        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH ||
               (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                
                String query = urlBar.getText().toString().trim();
                if (URLUtil.isValidUrl(query) || query.contains(".")) {
                    if (!query.startsWith("http://") && !query.startsWith("https://")) {
                        query = "https://" + query;
                    }
                    webView.loadUrl(query);
                } else {
                    webView.loadUrl("https://www.google.com/search?q=" + query);
                }
                return true;
            }
            return false;
        });

        // BUG 2 FIX: Incognito Toggle Logic
        incognitoToggle.setOnClickListener(v -> {
            isIncognito = !isIncognito;
            
            SharedPreferences prefs = getSharedPreferences("IncogPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("is_incognito", isIncognito).apply();

            Intent serviceIntent = new Intent(MainActivity.this, ClearService.class);

            if (isIncognito) {
                incognitoToggle.setText("🕵️"); 
                // Service aur Notification start karo
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            } else {
                incognitoToggle.setText("🔒"); 
                // Service aur Notification band karo
                stopService(serviceIntent);
            }
        });

        // Desktop Mode Toggle Logic
        desktopToggle.setOnClickListener(v -> {
            isDesktop = !isDesktop;
            WebSettings settings = webView.getSettings();
            if (isDesktop) {
                desktopToggle.setText("🖥️");
                settings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
            } else {
                desktopToggle.setText("📱");
                settings.setUserAgentString(null); 
            }
            webView.reload();
        });
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
    }
            }
