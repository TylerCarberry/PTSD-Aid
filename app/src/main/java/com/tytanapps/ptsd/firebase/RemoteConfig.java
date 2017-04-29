package com.tytanapps.ptsd.firebase;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.tytanapps.ptsd.BuildConfig;
import com.tytanapps.ptsd.R;

public class RemoteConfig {

    private static FirebaseRemoteConfig firebaseRemoteConfig;

    private static void setupRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
    }

    public static void fetchRemoteConfig(int cacheSeconds) {
       if(firebaseRemoteConfig == null) {
           setupRemoteConfig();
           cacheSeconds = 0;
       }

        firebaseRemoteConfig.fetch(cacheSeconds)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseRemoteConfig.activateFetched();
                    }
                });
    }

    public static FirebaseRemoteConfig getFirebaseRemoteConfig() {
        if(firebaseRemoteConfig == null) setupRemoteConfig();
        return firebaseRemoteConfig;
    }

    public static boolean getBoolean(@NonNull Context context, int resId) {
        if(firebaseRemoteConfig == null) setupRemoteConfig();
        return firebaseRemoteConfig.getBoolean(context.getString(resId));
    }

    public static int getInt(@NonNull Context context, int resId) {
        if(firebaseRemoteConfig == null) setupRemoteConfig();
        return (int) firebaseRemoteConfig.getDouble(context.getString(resId));
    }

    public static double getDouble(@NonNull Context context, int resId) {
        if(firebaseRemoteConfig == null) setupRemoteConfig();
        return firebaseRemoteConfig.getDouble(context.getString(resId));
    }

    public static String getString(@NonNull Context context, int resId) {
        if(firebaseRemoteConfig == null) setupRemoteConfig();
        return firebaseRemoteConfig.getString(context.getString(resId));
    }
}
