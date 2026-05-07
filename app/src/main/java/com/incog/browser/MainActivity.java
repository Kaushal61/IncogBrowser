package com.incog.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private WebView webView;
    private EditText urlBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, ClearService.class));

        urlBar = findViewById(R.id.urlBar);
        webView = findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setSaveFormData(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                    WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        urlBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null &&
                     event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String url = urlBar.getText().toString().trim();
                    if (!url.startsWith("http://") &&
                        !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    webView.loadUrl(url);
                    return true;
                }
                return false;
            }
        });
    }

    private void nukeAll() {
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        WebStorage.getInstance().deleteAllData();
        if (webView != null) {
            webView.clearCache(true);
            webView.clearHistory();
            webView.clearFormData();
            webView.clearSslPreferences();
            webView.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        nukeAll();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            nukeAll();
            super.onBackPressed();
        }
    }
                     }
