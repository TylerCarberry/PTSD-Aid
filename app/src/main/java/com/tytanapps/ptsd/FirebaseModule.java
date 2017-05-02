package com.tytanapps.ptsd;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.tytanapps.ptsd.firebase.RemoteConfig;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class FirebaseModule {

    public FirebaseModule() {
        //this.mainActivity = mainActivity;
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
    RemoteConfig providesRemoteConfig(FirebaseRemoteConfig firebaseRemoteConfig) {
        return new RemoteConfig(firebaseRemoteConfig);
    }

}