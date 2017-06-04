package com.tytanapps.ptsd;

import android.app.Application;

import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;
import com.tytanapps.ptsd.dagger.AppModule;
import com.tytanapps.ptsd.dagger.DaggerPtsdComponent;
import com.tytanapps.ptsd.dagger.PtsdComponent;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * The PTSD application. Used to connect the app with Google Analytics
 */
public class PTSDApplication extends Application {
    private PtsdComponent ptsdComponent;

    @Inject Tracker tracker;

    @Override
    public void onCreate() {
        ptsdComponent = DaggerPtsdComponent.builder()
                .appModule(new AppModule(this))
                .build();
        ptsdComponent.inject(this);

        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        Timber.plant(new Timber.DebugTree());
    }

    public PtsdComponent getPtsdComponent() {
        return ptsdComponent;
    }
}