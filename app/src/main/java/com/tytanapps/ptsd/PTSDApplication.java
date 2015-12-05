package com.tytanapps.ptsd;

import android.app.Application;

import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

public class PTSDApplication extends Application {
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();

        getDefaultTracker();
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            if (analytics != null) {
                analytics.enableAutoActivityReports(this);
                analytics.setLocalDispatchPeriod(60);

                // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
                mTracker = analytics.newTracker(R.xml.global_tracker);
                mTracker.enableAutoActivityTracking(true);

                ArrayList<String> packages = new ArrayList<>();
                packages.add("com.tytanapps.ptsd");

                ExceptionReporter reporter = new ExceptionReporter(mTracker, Thread.getDefaultUncaughtExceptionHandler(), this);
                reporter.setExceptionParser(new AnalyticsExceptionParser(this, packages));
                Thread.setDefaultUncaughtExceptionHandler(reporter);

                mTracker.enableExceptionReporting(true);
            }

        }
        return mTracker;
    }
}