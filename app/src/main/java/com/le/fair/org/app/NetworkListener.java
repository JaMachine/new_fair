package com.le.fair.org.app;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class NetworkListener extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(statusListener);
        return START_STICKY;
    }
    public static boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnectedOrConnecting()) return true;
        else return false;
    }
    Handler handler = new Handler();
    private Runnable statusListener = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(statusListener, 333);
            Intent intent = new Intent();
            intent.setAction(myNetworkStatus);
            intent.putExtra("online_status", "" + isConnected(NetworkListener.this));
            sendBroadcast(intent);
        }
    };
    public static String myNetworkStatus = "status";
}
