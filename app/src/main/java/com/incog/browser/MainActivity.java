package com.incog.browser;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;

public class MainActivity extends Activity {

    private WebView webView;
    private EditText urlBar;
    private Button btnIncognito;
    private Button btnDesktop;
    private LinearLayout rootLayout;

    private boolean isIncognito = false;
    private boolean isDesktop = false;

    private static final String MOBILE_UA =
        "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";

    private static final String DESKTOP_UA =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ClearService.class));

        rootLayout   = findViewById(R.id.rootLayout);
        urlBar       = findViewById(R.id.urlBar);
        webView      = findViewById(R.id.webView);
        btnIncognito = findViewById(R.id.btnIncognito);
        btnDesktop   = findViewById(R.id.btnDesktop);

        setupWebView();

        btnIncognito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isIncognito = !isIncognito;
                saveIncognitoState(isIncognito);
                applyIncognitoMode();
            }
        });

        btnDesktop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDesktop = !isDesktop;
                applyDesktopMode();
            }
        });

        urlBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null &&
                     event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    loadSmartUrl(urlBar.getText().toString().trim());
                    return true;
                }
                return false;
            }
        });
    }

    private void saveIncognitoState(boolean state) {
        SharedPreferences prefs = getSharedPreferences("incog_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("is_incognito", state).apply();
    }

    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setSaveFormData(false);
        s.setUserAgentString(MOBILE_UA);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                    WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });
    }

    private void loadSmartUrl(String input) {
        String url;
        if (input.contains(".") && !input.contains(" ")) {
            if (!input.startsWith("http://") &&
                !input.startsWith("https://")) {
                url = "https://" + input;
            } else {
                url = input;
            }
        } else {
            url = "https://www.google.com/search?q=" +
                  input.replace(" ", "+");
        }
        webView.loadUrl(url);
    }

    private void applyIncognitoMode() {
        WebSettings s = webView.getSettings();
        if (isIncognito) {
            btnIncognito.setText("🕵️");
            rootLayout.setBackgroundColor(Color.parseColor("#1A1A1A"));
            s.setDomStorageEnabled(false);
            s.setCacheMode(WebSettings.LOAD_NO_CACHE);
            CookieManager.getInstance().setAcceptCookie(false);
            webView.clearCache(true);
            webView.clearHistory();
        } else {
            nukeIncognitoData();
            btnIncognito.setText("🔒");
            rootLayout.setBackgroundColor(Color.WHITE);
            s.setDomStorageEnabled(true);
            s.setCacheMode(WebSettings.LOAD_DEFAULT);
            CookieManager.getInstance().setAcceptCookie(true);
        }
        webView.loadUrl("about:blank");
    }

    private void applyDesktopMode() {
        WebSettings s = webView.getSettings();
        if (isDesktop) {
            btnDesktop.setText("📱");
            s.setUserAgentString(DESKTOP_UA);
        } else {
            btnDesktop.setText("🖥️");
            s.setUserAgentString(MOBILE_UA);
        }
        webView.reload();
    }

    private void nukeIncognitoData() {
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        WebStorage.getInstance().deleteAllData();
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();
        webView.clearSslPreferences();
        deleteDir(getCacheDir());
        deleteDir(new File(getApplicationInfo().dataDir, "app_webview"));
    }

    private void deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) deleteDir(f);
            }
        }
        if (dir != null) dir.delete();
    }

    @Override
    protected void onDestroy() {
        if (isIncognito) {
            nukeIncognitoData();
            saveIncognitoState(false);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            if (isIncognito) {
                nukeIncognitoData();
                saveIncognitoState(false);
            }
            super.onBackPressed();
        }
    }
                     }
