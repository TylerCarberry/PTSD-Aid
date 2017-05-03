package com.tytanapps.ptsd;

import android.app.Application;

import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

/**
 * The PTSD application. Used to connect the app with Google Analytics
 */
public class PTSDApplication extends Application {
    private FirebaseComponent firebaseComponent;

    @Inject Tracker tracker;

    @Override
    public void onCreate() {
        firebaseComponent = DaggerFirebaseComponent.builder().appModule(new AppModule(this)).build();
        firebaseComponent.inject(this);

        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    public FirebaseComponent getFirebaseComponent() {
        return firebaseComponent;
    }
}