package com.tytanapps.ptsd.fragments;

import android.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tytanapps.ptsd.PTSDApplication;

/**
 * A fragment that sends screen hits to Google Analytics
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

    /**
     * Send an analytics event to Google Analytics
     * @param category The category of the event
     * @param action The action of the event
     */
    public void sendAnalyticsEvent(String category, String action) {
        // Obtain the shared Tracker instance.
        PTSDApplication application = (PTSDApplication) getActivity().getApplication();
        Tracker mTracker = application.getDefaultTracker();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
    }
}
