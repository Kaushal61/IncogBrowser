package com.incog.browser;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.webkit.CookieManager;
import java.io.File;

public class ClearService extends Service {

    private static final String CHANNEL_ID = "incog_clear";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundWithNotification();
        return START_NOT_STICKY;
    }

    private void startForegroundWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "IncogBrowser",
                NotificationManager.IMPORTANCE_MIN
            );
            channel.setShowBadge(false);
            NotificationManager nm =
                getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("IncogBrowser")
            .setContentText("Incognito mode active")
            .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
            .build();
        startForeground(1, notification);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        // Sirf incognito ON hone pe clear karo
        SharedPreferences prefs = getSharedPreferences(
            "incog_prefs", MODE_PRIVATE);
        boolean wasIncognito = prefs.getBoolean("is_incognito", false);

        if (wasIncognito) {
            try {
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
                File dataDir = new File(getApplicationInfo().dataDir);
                deleteDir(new File(dataDir, "app_webview"));
                deleteDir(new File(dataDir, "cache"));
                deleteDir(getCacheDir());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // State reset karo
            prefs.edit().putBoolean("is_incognito", false).apply();
        }

        stopForeground(true);
        stopSelf();
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
                }
