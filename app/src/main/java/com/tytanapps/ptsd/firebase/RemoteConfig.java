package com.tytanapps.ptsd.firebase;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

/**
 * A wrapper for FirebaseRemoteConfig
 */
public class RemoteConfig {

    private FirebaseRemoteConfig firebaseRemoteConfig;

    public RemoteConfig(@NonNull FirebaseRemoteConfig firebaseRemoteConfig) {
        this.firebaseRemoteConfig = firebaseRemoteConfig;
    }

    public void fetch(int cacheSeconds) {
        firebaseRemoteConfig.fetch(cacheSeconds)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseRemoteConfig.activateFetched();
                    }
                });
    }

    public FirebaseRemoteConfig getFirebaseRemoteConfig() {
        return firebaseRemoteConfig;
    }

    public boolean getBoolean(@NonNull Context context, int resId) {
        return firebaseRemoteConfig.getBoolean(context.getString(resId));
    }

    public int getInt(@NonNull Context context, int resId) {
        return (int) firebaseRemoteConfig.getDouble(context.getString(resId));
    }

    public double getDouble(@NonNull Context context, int resId) {
        return firebaseRemoteConfig.getDouble(context.getString(resId));
    }

    public String getString(@NonNull Context context, int resId) {
        return firebaseRemoteConfig.getString(context.getString(resId));
    }
}
