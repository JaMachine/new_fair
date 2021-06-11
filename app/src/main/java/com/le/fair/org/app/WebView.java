package com.le.fair.org.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pl.droidsonroids.gif.GifImageView;

import static com.le.fair.org.app.NetworkListener.myNetworkStatus;
import static com.le.fair.org.app.MainActivity.mySource;

public class WebView extends AppCompatActivity {
    protected static int requestCode = 1;
    protected static int resultCode = 1;
    protected android.webkit.WebView myView;
    protected boolean myOnline;
    protected Uri myCameraFolder = null;
    protected GifImageView myInternetStatus;
    protected ValueCallback<Uri> myUploadMsg;
    protected ValueCallback<Uri[]> myFilesFolder;
    protected String myPhotosFolder;
    protected IntentFilter myFilter;


    public class myClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
            try {
                if (Uri.parse(url).getHost().contains(MainActivity.getString("cGludGVyZXN0")))
                    return true;
                if (Uri.parse(url).getHost().contains(MainActivity.getString("dHdpdHRlcg==")))
                    return true;
                if (Uri.parse(url).getHost().contains(MainActivity.getString("ZmFjZWJvb2s=")))
                    return true;
                if (Uri.parse(url).getHost().contains(MainActivity.getString("aW5zdGFncmFt")))
                    return true;
                if (Uri.parse(url).getHost().contains(MainActivity.getString("eW91dHViZQ==")))
                    return true;
                if (Uri.parse(url).getHost().contains(MainActivity.getString("bGlua2Vk")))
                    return true;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        myView = findViewById(R.id.wv);
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(WebView.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }
        myView.setWebViewClient(new myClient());
        myView.getSettings().setAllowFileAccess(true);
        myView.getSettings().setDomStorageEnabled(true);
        myView.getSettings().setJavaScriptEnabled(true);
        myView.getSettings().setLoadsImagesAutomatically(true);
        if (savedInstanceState != null)
            myView.restoreState(savedInstanceState.getBundle("webViewState"));

        myView.setWebChromeClient(new WebChromeClient() {
            private File createImageFile() throws IOException {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File imageFile = File.createTempFile(
                        imageFileName,
                        ".jpg",
                        storageDir
                );
                return imageFile;

            }

            public boolean onShowFileChooser(android.webkit.WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
                if (myFilesFolder != null) {
                    myFilesFolder.onReceiveValue(null);
                }
                myFilesFolder = filePath;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", myPhotosFolder);
                    } catch (IOException e) {
                        e.fillInStackTrace();
                    }
                    if (photoFile != null) {
                        myPhotosFolder = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, requestCode);
                return true;
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                myUploadMsg = uploadMsg;
                File imageStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES)
                        , "AndroidExampleFolder");
                if (!imageStorageDir.exists()) {
                    imageStorageDir.mkdirs();
                }
                File file = new File(
                        imageStorageDir + File.separator + "IMG_"
                                + String.valueOf(System.currentTimeMillis())
                                + ".jpg");
                myCameraFolder = Uri.fromFile(file);
                final Intent captureIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, myCameraFolder);
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                        , new Parcelable[]{captureIntent});
                startActivityForResult(chooserIntent, resultCode);
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType,
                                        String capture) {
                openFileChooser(uploadMsg, acceptType);
            }

        });
        myFilter = new IntentFilter();
        myFilter.addAction(myNetworkStatus);
        Intent intent = new Intent(this, NetworkListener.class);
        startService(intent);
        myInternetStatus = findViewById(R.id.wv_internet_status);
        if (isConnected(getApplicationContext()))
            connectedView();
        else disconnectedMsg();

        fullScreen();
    }

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(myNetworkStatus)) {
                if (intent.getStringExtra("online_status").equals("true"))
                    connectedView();
                else disconnectedMsg();
            }
        }
    };

    void disconnectedMsg() {
        myInternetStatus.setVisibility(View.VISIBLE);
        myView.setVisibility(View.GONE);
        myOnline = false;
    }

    public void connectedView() {
        if (!myOnline) {
            myInternetStatus.setVisibility(View.GONE);
            myView.loadUrl(mySource);
            myView.setVisibility(View.VISIBLE);
            myOnline = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != WebView.requestCode || myFilesFolder == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    if (myPhotosFolder != null) {
                        results = new Uri[]{Uri.parse(myPhotosFolder)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }
            myFilesFolder.onReceiveValue(results);
            myFilesFolder = null;
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != WebView.resultCode || myUploadMsg == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            if (requestCode == WebView.resultCode) {
                if (null == this.myUploadMsg) {
                    return;
                }
                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        result = data == null ? myCameraFolder : data.getData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                myUploadMsg.onReceiveValue(result);
                myUploadMsg = null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (myView.canGoBack()) myView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onResume() {
        fullScreen();
        registerReceiver(broadcastReceiver, myFilter);
        super.onResume();
    }

    @Override
    protected void onRestart() {
        registerReceiver(broadcastReceiver, myFilter);
        fullScreen();
        super.onRestart();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        fullScreen();
        super.onPause();
    }

    public boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnectedOrConnecting())
            return true;
        else
            return false;
    }

    private void fullScreen() {
        View v = findViewById(R.id.wv_container);
        v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}