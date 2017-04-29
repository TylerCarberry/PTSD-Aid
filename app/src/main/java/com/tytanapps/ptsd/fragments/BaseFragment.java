package com.tytanapps.ptsd.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tytanapps.ptsd.MainActivity;
import com.tytanapps.ptsd.PTSDApplication;
import com.tytanapps.ptsd.R;

import butterknife.Unbinder;

/**
 * A fragment that sends screen hits to Google Analytics
 */
public abstract class BaseFragment extends Fragment {

    protected Unbinder unbinder;

    protected abstract @StringRes int getTitle();

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(getTitle());

        // Obtain the shared Tracker instance.
        PTSDApplication application = (PTSDApplication) getActivity().getApplication();

        // Send a screen hit to Google Analytics with the name of the current activity
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName(getClass().getSimpleName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    /**
     * Open the navigation drawer
     */
    protected void openDrawer() {
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.openDrawer(Gravity.LEFT);
    }

    protected void signInGoogle() {
        Activity parentActivity = getActivity();
        if (parentActivity instanceof MainActivity) {
            ((MainActivity) getActivity()).signInGoogle();
        }
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

    /**
     * @return The root view of the fragment casted to a ViewGroup
     */
    protected ViewGroup getRootViewGroup() {
        View rootView = getView();
        if (rootView instanceof ViewGroup) {
            return (ViewGroup) getView();
        }
        return null;
    }

    protected void setCheckedNavigationItem(@IdRes int resId) {
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(resId).setChecked(true);
    }

    /**
     * Get a shared preference String from a saved file
     * @param prefKey The key of the String
     * @param defaultValue The default value if no key exists
     * @return The shared preference String with the given key
     */
    protected String getSharedPreferenceString(String prefKey, String defaultValue) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(prefKey, defaultValue);
    }

}
