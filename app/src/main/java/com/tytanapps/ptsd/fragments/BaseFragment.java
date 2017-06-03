package com.tytanapps.ptsd.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.tytanapps.ptsd.MainActivity;
import com.tytanapps.ptsd.PTSDApplication;
import com.tytanapps.ptsd.R;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;

import static butterknife.ButterKnife.findById;

/**
 * A fragment that sends screen hits to Google Analytics
 */
public abstract class BaseFragment extends Fragment {

    protected Unbinder unbinder;

    @Inject Tracker tracker;

    protected abstract @StringRes int getTitle();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        getApplication().getPtsdComponent().inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(getRootView(), container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    public @LayoutRes int getRootView() {
        return -1;
    }

    @Override
    public void onStart() {
        super.onStart();
        @IdRes int navigationItemId = getNavigationItem();
        if (navigationItemId != -1) {
            setCheckedNavigationItem(navigationItemId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(getTitle());

        // Send a screen hit to Google Analytics with the name of the current activity
        tracker.setScreenName(getClass().getSimpleName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
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
        tracker.send(new HitBuilders.EventBuilder()
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

    protected @IdRes int getNavigationItem() {
        return -1;
    }

    protected void setCheckedNavigationItem(@IdRes int resId) {
        NavigationView navigationView = findById(getActivity(), R.id.nav_view);
        navigationView.getMenu().findItem(resId).setChecked(true);
    }

    protected PTSDApplication getApplication() {
        return ((PTSDApplication)getActivity().getApplication());
    }

}
