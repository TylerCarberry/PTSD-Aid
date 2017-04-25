package com.tytanapps.ptsd.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;


public class PermissionUtil {

    public static final int REQUEST_CONTACT_PERMISSION = 301;
    public static final int REQUEST_LOCATION_PERMISSION = 302;

    /**
     * @return Whether the user has granted the READ_CONTACTS permission
     */
    public static boolean contactsPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request the contacts permission if it has not been granted
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void requestContactsPermission(Activity activity) {
        if (!contactsPermissionGranted(activity)) {
            activity.requestPermissions(
                    new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CONTACT_PERMISSION);
        }
    }

    /**
     * @return Whether the location permission has been granted
     */
    public static boolean locationPermissionGranted(Activity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request the location permission
     * Only needed in Android version 6.0 and up
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void requestLocationPermission(Activity activity) {
        if (!locationPermissionGranted(activity)) {
            activity.requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }
}
