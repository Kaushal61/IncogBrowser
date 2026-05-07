package com.incog.browser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

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

        webView = findViewById(R.id.webView);
        urlBar = findViewById(R.id.urlBar);
        incognitoToggle = findViewById(R.id.btnIncognito);
        desktopToggle = findViewById(R.id.btnDesktop);

        // App start hote hi pehle mode check karega
        SharedPreferences prefs = getSharedPreferences("IncogPrefs", MODE_PRIVATE);
        isIncognito = prefs.getBoolean("is_incognito", false);
        
        setupWebView();
        updateBrowserMode(); // Storage/Cookies ko mode ke hisaab se set karega

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

        incognitoToggle.setOnClickListener(v -> {
            isIncognito = !isIncognito;
            prefs.edit().putBoolean("is_incognito", isIncognito).apply();

            Intent serviceIntent = new Intent(MainActivity.this, ClearService.class);
            updateBrowserMode(); // Click karte hi mode update hoga

            if (isIncognito) {
                incognitoToggle.setText("🕵️"); 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            } else {
                incognitoToggle.setText("🔒"); 
                stopService(serviceIntent);
            }
        });

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
        webView.setWebViewClient(new WebViewClient());
    }

    // YEH FUNCTION DECIDE KAREGA KI KAB DATA SAVE KARNA HAI AUR KAB NAHI
    private void updateBrowserMode() {
        WebSettings settings = webView.getSettings();
        CookieManager cookieManager = CookieManager.getInstance();

        if (isIncognito) {
            // INCOGNITO MODE: Storage band, history clear. Normal mode ko touch nahi karega.
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            settings.setDomStorageEnabled(false);
            settings.setDatabaseEnabled(false);
            cookieManager.setAcceptCookie(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(webView, false);
            }
            webView.clearHistory();
            webView.clearCache(true);
        } else {
            // NORMAL MODE: Chrome ki tarah har tarah ka data permanently save karega
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setDomStorageEnabled(true);
            settings.setDatabaseEnabled(true);
            cookieManager.setAcceptCookie(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(webView, true);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush(); // App background me jaate hi data phone storage me force save
    }
    }
