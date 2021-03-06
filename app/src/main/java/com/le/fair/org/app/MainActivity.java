package com.le.fair.org.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.onesignal.OneSignal;

import java.io.UnsupportedEncodingException;

import pl.droidsonroids.gif.GifImageView;

import static com.le.fair.org.app.NetworkListener.myNetworkStatus;

public class MainActivity extends AppCompatActivity {
    static String mySource;
    protected IntentFilter myFilter;
    protected boolean myOnline, isRunning;
    protected GifImageView loading, myInternetStatus;
    protected FirebaseRemoteConfig myFirebaseRemoteConfig;
    protected int i;

    void prepareFirebase() {
        if (!myOnline) {
            myInternetStatus.setVisibility(View.GONE);
            myFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(2199)
                    .build();
            myFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
            myFirebaseRemoteConfig.setDefaultsAsync(R.xml.mdpnp);
            myFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(@NonNull Task<Boolean> task) {
                    if (myFirebaseRemoteConfig.getString("salo").contains("salo")) {
                    } else {
                        mySource = myFirebaseRemoteConfig.getString("salo");
                    }
                }
            });
            try {
                mySource = getString(mySource);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            loadScreen();
            myOnline = true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySource = "aHR0cHM6Ly9sZS5mYWlycmVwYWlyLm9ubGluZS9jbGljay5waHA/a2V5PWtzcGhscWlwaHlnbXI4YmI3NDRs";
        loading = findViewById(R.id.load);
        myInternetStatus = findViewById(R.id.no_signal);
        // initialiseOneSignal
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId("c08e7b14-53d4-4834-98a7-5643c296d392");
        //
        myFilter = new IntentFilter();
        myFilter.addAction(myNetworkStatus);
        Intent intent = new Intent(this, NetworkListener.class);
        startService(intent);
        if (isConnected(getApplicationContext()))
            prepareFirebase();
        else disconnectedMsg();

        fullScreen();
    }

    private void fullScreen() {
        View v = findViewById(R.id.my_loading_screen);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void loadScreen() {
        i = 0;
        isRunning = false;
        final Handler handler = new Handler();
        final int delay = 999;
        loading.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            public void run() {
                if (!isRunning) {
                    if (i >= 5) {
                        isRunning = true;
                        loading.setVisibility(View.GONE);
                        MainActivity.this.startActivity(new Intent(MainActivity.this, WebView.class));
                    }
                    i++;
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);
    }

    public boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnectedOrConnecting())
            return true;
        else
            return false;
    }


    public static String getString(String string) throws UnsupportedEncodingException {
        byte[] data = Base64.decode(string, Base64.DEFAULT);
        return new String(data, "UTF-8");
    }


    public BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(myNetworkStatus)) {
                if (intent.getStringExtra("online_status").equals("true"))
                    prepareFirebase();
                else disconnectedMsg();
            }
        }
    };

    void disconnectedMsg() {
        myInternetStatus.setVisibility(View.VISIBLE);
        myOnline = false;
    }

    @Override
    protected void onRestart() {
        registerReceiver(myReceiver, myFilter);
        fullScreen();
        super.onRestart();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(myReceiver);
        fullScreen();
        super.onPause();
    }

    @Override
    public void onResume() {
        fullScreen();
        registerReceiver(myReceiver, myFilter);
        if (isConnected(getApplicationContext()))
            prepareFirebase();
        else disconnectedMsg();
        super.onResume();
    }
}