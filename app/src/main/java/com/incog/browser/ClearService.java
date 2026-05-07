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

public class ClearService extends Service {
    private static final String CHANNEL_ID = "IncognitoChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        builder.setContentTitle("Incognito Mode ON")
               .setContentText("Your browsing data won't be saved.")
               .setSmallIcon(android.R.drawable.ic_secure); 

        startForeground(1, builder.build());
        return START_NOT_STICKY; 
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        
        SharedPreferences prefs = getSharedPreferences("IncogPrefs", MODE_PRIVATE);
        boolean isIncognito = prefs.getBoolean("is_incognito", false);

        if (isIncognito) {
            // Sirf session cookies hatayega. Normal mode ka data aur YouTube logins bilkul safe rahenge!
            CookieManager.getInstance().removeSessionCookies(null);
            CookieManager.getInstance().flush();
            prefs.edit().putBoolean("is_incognito", false).apply();
        }
        
        stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Incognito Mode",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
    }
