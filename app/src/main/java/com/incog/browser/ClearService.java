package com.incog.browser;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.io.File;

public class ClearService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        try {
            File dataDir = new File(getApplicationInfo().dataDir);
            deleteDir(new File(dataDir, "app_webview"));
            deleteDir(getCacheDir());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
