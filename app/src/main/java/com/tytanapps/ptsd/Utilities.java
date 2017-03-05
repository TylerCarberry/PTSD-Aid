package com.tytanapps.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Base64;
import android.widget.Toast;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * A collection of methods that do not apply to a specific fragment
 */
public class Utilities {

    /**
     * Crop a bitmap to a circle and scale it
     * @param bitmap The bitmap to crop
     * @param width The width of the new bitmap
     * @param height The height of the new bitmap
     * @return The bitmap scaled and cropped to a circle
     */
    public static Bitmap getCircularBitmap(Bitmap bitmap, int width, int height) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return Bitmap.createScaledBitmap(output, width, height, false);
    }

    /**
     * Calculate the distance between coordinates in miles
     * @param lat1 Latitude of coordinate 1
     * @param lon1 Longitude of coordinate 1
     * @param lat2 Latitude of coordinate 2
     * @param lon2 Longitude of coordinate 2
     * @return The distance between the two coordinates in the specified unit
     */
    public static double distanceBetweenCoordinates(double lat1, double lon1, double lat2, double lon2) {
        float[] result = {0, 0};
        Location.distanceBetween(lat1, lon1, lat2, lon2, result);
        return metersToMiles(result[0]);
    }

    public static double metersToMiles(float meters) {
        return meters * 0.000621371192;
    }

    public static double degreesToRadians(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public static double radiansToDegrees(double rad) {
        return (rad * 180 / Math.PI);
    }

    /**
     * Get the user's GPS location
     * @return The GPS coordinates: latitude, longitude. If the location cannot be determined, 0,0
     */
    public static double[] getGPSLocation(Activity activity) {
        double[] gps = new double[2];

        if(activity != null) {
            LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = lm.getProviders(true);

            Location l = null;

            // Try getting the location from each of the location providers
            for (int i = providers.size() - 1; i >= 0; i--) {
                try {
                    l = lm.getLastKnownLocation(providers.get(i));
                } catch (SecurityException e) {
                    // The user has blocked location
                }
                if (l != null) {
                    gps[0] = l.getLatitude();
                    gps[1] = l.getLongitude();
                    break;
                }
            }
        }
        return gps;
    }

    /**
     * Save a bitmap to a file
     * @param file The file to save to
     * @param bitmap The bitmap to save
     */
    public static void saveBitmapToFile(File file, Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load a bitmap from a file
     * @param file The file to read from
     * @return The bitmap saved at that location, null if the file does not exist
     */
    public static Bitmap loadBitmapFromFile(File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    /**
     * Determine the first phone number in the String
     * The Facility API returns multiple phone numbers with an or between them
     * @param phoneNumbers A string of one or more phone numbers
     * @return The first phone number in the string
     */
    public static String getFirstPhoneNumber(String phoneNumbers) {
        if(phoneNumbers == null)
            return null;

        int orLocation = phoneNumbers.indexOf(" Or");

        if(orLocation >= 0)
            return phoneNumbers.substring(0, orLocation);
        return phoneNumbers;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Decode a bitmap from a base64 string
     * @param bitmap_base64 The encoded bitmap
     * @return The decoded bitmap
     */
    public static Bitmap decodeBitmap(String bitmap_base64) {
        byte[] imageAsBytes = Base64.decode(bitmap_base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    public static Bitmap base64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public static String drawableToBase64 (Drawable drawable) {
        Bitmap bitmap = Utilities.drawableToBitmap(drawable);
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Get the FirebaseRemoteConfig of a fragment's parent activity
     * PRECONDITION: The activity containing fragment implements RemoteConfigurable
     * @param fragment The fragment to get the remote config from
     * @return The FirebaseRemoteConfig of fragment's activity
     */
    private static FirebaseRemoteConfig getRemoteConfig(@NonNull Fragment fragment) {
        return ((RemoteConfigurable)fragment.getActivity()).getRemoteConfig();
    }

    public static boolean getRemoteConfigBoolean(@NonNull Fragment fragment, int resId) {
        FirebaseRemoteConfig firebaseRemoteConfig = getRemoteConfig(fragment);
        return firebaseRemoteConfig.getBoolean(fragment.getString(resId));
    }

    public static int getRemoteConfigInt(@NonNull Fragment fragment, int resId) {
        FirebaseRemoteConfig firebaseRemoteConfig = getRemoteConfig(fragment);
        return (int) firebaseRemoteConfig.getDouble(fragment.getString(resId));
    }

    public static double getRemoteConfigDouble(@NonNull Fragment fragment, int resId) {
        FirebaseRemoteConfig firebaseRemoteConfig = getRemoteConfig(fragment);
        return firebaseRemoteConfig.getDouble(fragment.getString(resId));
    }

    public static String getRemoteConfigString(@NonNull Fragment fragment, int resId) {
        FirebaseRemoteConfig firebaseRemoteConfig = getRemoteConfig(fragment);
        return firebaseRemoteConfig.getString(fragment.getString(resId));
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
     * Open the dialer with a phone number entered
     * This does not call the number directly, the user needs to press the call button
     * @param phoneNumber The phone number to call
     */
    public static void openDialer(Fragment fragment, String phoneNumber) {

        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            fragment.startActivity(intent);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(fragment.getActivity(), R.string.error_open_dialer, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open a website in the browser
     * Precondition: url is a valid url
     * @param url The url to open
     */
    public static void openBrowserIntent(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
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

        /*
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        fragment.startActivity(i);
        */
    }

    /**
     * Open the maps app to a specified location
     * @param geoLocation The uri of the location to open
     */
    public static void openMapIntent(Context context, Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
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

    @SuppressWarnings("deprecation")
    public static String htmlToText(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(source).toString();
        }
    }

    public static String readFromUrl(String urlString) throws IOException {
        String response = "";

        URL url = new URL(urlString);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            response += line;
        }
        reader.close();

        return response;
    }

    public static Bitmap readBitmapFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        return BitmapFactory.decodeStream(url.openConnection().getInputStream());
    }


}
