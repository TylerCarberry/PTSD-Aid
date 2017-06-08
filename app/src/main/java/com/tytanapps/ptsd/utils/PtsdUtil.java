package com.tytanapps.ptsd.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.provider.ContactsContract;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.crash.FirebaseCrash;
import com.tytanapps.ptsd.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * A collection of methods that do not apply to a specific fragment
 */
public class PtsdUtil {

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

        if (activity != null) {
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
        if (phoneNumbers == null)
            return null;

        int orLocation = phoneNumbers.indexOf(" Or");

        if (orLocation >= 0)
            return phoneNumbers.substring(0, orLocation);
        return phoneNumbers;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
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
        Bitmap bitmap = PtsdUtil.drawableToBitmap(drawable);
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @SuppressWarnings("deprecation")
    public static String htmlToText(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(source).toString();
        }
    }

    public static String readFromUrl(OkHttpClient client, String urlString) throws IOException {
        String response = "";

        URL url = new URL(urlString);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            response += line;
        }
        reader.close();
        return response;

        /*Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string(); */
    }

    public static Bitmap readBitmapFromUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        return BitmapFactory.decodeStream(url.openConnection().getInputStream());
    }

    public static boolean isVeteran(Activity activity) {
        return activity.getPreferences(Context.MODE_PRIVATE).getBoolean(activity.getString(R.string.pref_veteran), true);
    }

    public static void dismissKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * @return debug information about the phone and app
     */
    public static String getDeviceInformation(Context context) {
        String deviceInformation = "";

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;

            deviceInformation += "APP VERSION: " + version + " (" + verCode + ")\n";

        } catch (PackageManager.NameNotFoundException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }

        deviceInformation += "SDK INT: " + Build.VERSION.RELEASE + " (" + Build.VERSION.SDK_INT + ")\n" +
                "CODENAME: " + Build.VERSION.CODENAME + "\n" +
                "INCREMENTAL: " + Build.VERSION.INCREMENTAL + "\n" +
                "RELEASE: " + Build.VERSION.RELEASE + "\n" +
                "BOARD: " + Build.BOARD + "\n" +
                "BOOTLOADER: " + Build.BOOTLOADER + "\n" +
                "BRAND: " + Build.BRAND + "\n" +
                "DEVICE: " + Build.DEVICE + "\n" +
                "DISPLAY: " + Build.DISPLAY + "\n" +
                "FP: " + Build.FINGERPRINT + "\n" +
                "RADIO VERSION: " + Build.getRadioVersion() + "\n" +
                "HARDWARE: " + Build.HARDWARE + "\n" +
                "HOST: " + Build.HOST + "\n" +
                "ID: " + Build.ID + "\n" +
                "MANUFACTURER: " + Build.MANUFACTURER + "\n" +
                "MODEL: " + Build.MODEL + "\n" +
                "PRODUCT: " + Build.PRODUCT + "\n" +
                "SERIAL: " + Build.SERIAL + "\n" +
                "TAGS: " + Build.TAGS + "\n" +
                "TYPE: " + Build.TYPE + "\n" +
                "UNKNOWN: " + Build.UNKNOWN + "\n" +
                "USER: " + Build.USER + "\n" +
                "TIME: " + Build.TIME + "\n";

        return deviceInformation;
    }


    /**
     * Get a contact's name given their phone number
     * @param phoneNumber The phone number of the contact
     * @return The contact's name
     */
    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        String contactName = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return contactName;
    }

    public static List<View> getLayoutChildren(ViewGroup viewGroup) {
        List<View> children = new ArrayList<>();

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            children.add(child);
        }

        return children;
    }


}
