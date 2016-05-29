package com.tytanapps.ptsd;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

/**
 * Created by Tyler on 5/28/16.
 */
public interface RemoteConfigurable {

    public FirebaseRemoteConfig getRemoteConfig();
}
