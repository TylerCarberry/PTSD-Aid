package com.tytanapps.ptsd;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.tytanapps.ptsd.firebase.RemoteConfig;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;

@Module
public class PtsdModule {

    public PtsdModule() {

    }

    @Provides
    @Singleton
    FirebaseRemoteConfig providesFirebaseRemoteConfig() {
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        return firebaseRemoteConfig;
    }

    @Provides
    @Singleton
    RemoteConfig providesRemoteConfig(FirebaseRemoteConfig firebaseRemoteConfig, Context context) {
        return new RemoteConfig(firebaseRemoteConfig, context);
    }


    @Provides
    @Singleton
    GoogleSignInOptions providesGoogleSignInOptions() {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
    }

    @Provides
    @Singleton
    GoogleAnalytics providesGoogleAnalytics(PTSDApplication application) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(application);
        analytics.enableAutoActivityReports(application);
        analytics.setLocalDispatchPeriod(60);

        return analytics;
    }

    @Provides
    @Singleton
    Tracker providesTracker(GoogleAnalytics analytics) {
        Tracker tracker = analytics.newTracker(R.xml.global_tracker);
        tracker.enableAutoActivityTracking(true);
        return tracker;
    }

    @Provides
    @Singleton
    Cache providesOkHttpCache(PTSDApplication application) {
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        return new Cache(application.getCacheDir(), cacheSize);
    }

    @Provides
    @Singleton
    OkHttpClient providesOkHttpClient(Cache cache) {
         return new OkHttpClient.Builder()
                .cache(cache)
                .build();
    }

    @Provides
    @Singleton
    FirebaseDatabase providesFirebaseDatabase() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        return firebaseDatabase;
    }

    @Provides
    @Singleton
    FirebaseMessaging providesFirebaseMessaging() {
        return FirebaseMessaging.getInstance();
    }

    @Provides
    @Singleton
    FirebasePerformance providesFirebasePerformance() {
        return FirebasePerformance.getInstance();
    }

    @Provides
    @Singleton
    Preferences providesPreference(Context context) {
        return new Preferences(context);
    }

}