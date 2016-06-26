package com.tytanapps.ptsd.fragments;

import android.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tytanapps.ptsd.PTSDApplication;

/**
 * Created by Tyler on 6/17/16.
 */
public abstract class AnalyticsFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();

        // Obtain the shared Tracker instance.
        PTSDApplication application = (PTSDApplication) getActivity().getApplication();

        // Send a screen hit to Google Analytics with the name of the current activity
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName(getClass().getSimpleName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
