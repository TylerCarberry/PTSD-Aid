package com.tytanapps.ptsd;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
     * Calculate the distance between coordinates
     * @param lat1 Latitude of coordinate 1
     * @param lon1 Longitude of coordinate 1
     * @param lat2 Latitude of coordinate 2
     * @param lon2 Longitude of coordinate 2
     * @param unit The unit that the result should be in. (M)iles (K)ilometers (N)autical Miles
     * @return The distance between the two coordinates in the specified unit
     */
    public static double distanceBetweenCoordinates(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double distance =
                Math.sin(degreesToRadians(lat1)) * Math.sin(degreesToRadians(lat2))
                        + Math.cos(degreesToRadians(lat1)) * Math.cos(degreesToRadians(lat2)) * Math.cos(degreesToRadians(theta));

        distance = Math.acos(distance);
        distance = radiansToDegrees(distance);

        // Miles
        distance = distance * 60 * 1.1515;

        // Kilometers
        if (unit.equalsIgnoreCase("K"))
            distance = distance * 1.609344;
            // Nautical Miles
        else if (unit.equalsIgnoreCase("N"))
            distance = distance * 0.8684;

        return distance;
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
                    gps[0] = l.getLatitude()
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
}
