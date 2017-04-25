package com.tytanapps.ptsd.utils;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.tytanapps.ptsd.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ExternalAppUtil {

    /**
     * @return the APK version of the current app. -1 if it cannot be determined
     */
    public static int getApkVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Open the dialer with a phone number entered
     * This does not call the number directly, the user needs to press the call button
     * @param phoneNumber The phone number to call
     */
    public static void openDialer(Context context, String phoneNumber) {

        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            context.startActivity(intent);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(context.getApplicationContext(), R.string.error_open_dialer, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open a website in the browser
     * Precondition: url is a valid url
     * @param url The url to open
     */
    public static void openBrowserIntent(Fragment fragment, String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(fragment.getActivity(), R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(fragment.getActivity(), Uri.parse(url));
    }

    /**
     * Open the maps app to a specified location
     * @param geoLocation The uri of the location to open
     */
    public static void openMapIntent(Fragment fragment, Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
            fragment.startActivity(intent);
        }
    }

    /**
     * Get the url for the Google Maps Api
     * @param name The street address
     * @param town The town
     * @param state The state. Can be initials or full name
     * @return The url for the street view api
     * @throws UnsupportedEncodingException If the address cannot be encoded into a url
     */
    public static Uri getMapUri(String name, String town, String state) throws UnsupportedEncodingException {
        // Encode the address
        String location = name + ", " + town + ", " + state;
        location = URLEncoder.encode(location, "UTF-8");

        return Uri.parse("geo:0,0?q=" + location);
    }

    /**
     * Get the url for the Google Maps Api
     * @param location The location to encode
     * @return The url for the street view api
     * @throws UnsupportedEncodingException If the address cannot be encoded into a url
     */
    public static Uri getMapUri(String location) throws UnsupportedEncodingException {
        // Encode the address
        location = URLEncoder.encode(location, "UTF-8");

        return Uri.parse("geo:0,0?q=" + location);
    }
}
