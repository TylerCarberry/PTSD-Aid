package com.tytanapps.ptsd.firebase;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

/**
 * Classes that utilize Firebase Remote Configuration
 */
public interface RemoteConfigurable {

    FirebaseRemoteConfig getRemoteConfig();
}
