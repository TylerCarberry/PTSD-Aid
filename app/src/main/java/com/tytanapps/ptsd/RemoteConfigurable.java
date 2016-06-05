package com.tytanapps.ptsd;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

/**
 * Classes that utilize Firebase Remote Configuration
 */
public interface RemoteConfigurable {

    public FirebaseRemoteConfig getRemoteConfig();
}
