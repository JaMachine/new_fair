package com.le.fair.org.app;

import android.app.Application;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

public class Metrica extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        YandexMetricaConfig conf = YandexMetricaConfig.newConfigBuilder("2d15e03b-2a75-4477-ab38-d2b7edb3deef").build();
        YandexMetrica.activate(getApplicationContext(), conf);
        YandexMetrica.enableActivityAutoTracking(this);
    }

}
