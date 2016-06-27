package com.tytanapps.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.tytanapps.ptsd.Utilities.getRemoteConfigInt;

/**
 * Created by Tyler on 6/26/16.
 */
public class FacilityLoader {
    private static final String LOG_TAG = FacilityLoader.class.getSimpleName();

    private Fragment fragment;

    public FacilityLoader(Fragment fragment) {
        this.fragment = fragment;
    }

    /**
     * Load the facility image and place it into facilityImageView.
     * Try the street view image first, then the map image, then the default image.
     * @param facility The facility
     */
    public void loadFacilityImage(Facility facility) {
        Bitmap cachedBitmap = loadCacheFacilityImage(facility.getFacilityId());

        if(cachedBitmap != null)
            facility.setFacilityImage(cachedBitmap);
        else {
            int imageWidth = getRemoteConfigInt(fragment, R.string.rc_map_width);
            int imageHeight = getRemoteConfigInt(fragment, R.string.rc_map_height);

            loadStreetViewImage(facility, imageWidth, imageHeight);
        }

        //mAdapter.notifyDataSetChanged();
    }

    /**
     * Load the street view imagery for the given address.
     * If there is no street view imagery, it uses the map view instead.
     * You should not call this directly. Call loadFacilityImage instead
     * @param facility The facility
     */
    private void loadStreetViewImage(final Facility facility, final int imageWidth, final int imageHeight) {
        String url = "";

        // If the street view url cannot be created, load the map view instead
        try {
            url = calculateStreetViewAPIUrl(facility.getStreetAddress(), facility.getCity(), facility.getState(), imageWidth, imageHeight);
        } catch (UnsupportedEncodingException e) {
            FirebaseCrash.report(e);
            loadMapImage(facility, imageWidth, imageHeight);
            return;
        }

        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        // If there is no street view image for the address use the map view instead
                        if(validStreetViewBitmap(bitmap)) {
                            facility.setFacilityImage(bitmap);
                            saveFacilityImage(bitmap, facility.getFacilityId());
                        }
                        else
                            loadMapImage(facility, imageWidth, imageHeight);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "Street View Image errorListener: " + error.toString());

                        // Load the map view instead
                        loadMapImage(facility, imageWidth, imageHeight);
                    }
                });

        // Start loading the image in the background
        RequestQueue requestQueue = getRequestQueue();
        if(requestQueue != null)
            requestQueue.add(request);
    }

    /**
     * Load the Google Maps imagery for the given address.
     * If there is no map imagery, it uses the default image instead.
     * You should not call this directly. Call loadFacilityImage instead
     * @param facility The facility
     */
    private void loadMapImage(final Facility facility, int imageWidth, int imageHeight) {
        final int defaultImageId = R.drawable.default_facility_image;

        String url;
        try {
            url = calculateMapAPIUrl(facility.getStreetAddress(), facility.getCity(), facility.getState(), imageWidth, imageHeight);
        } catch (UnsupportedEncodingException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
            facility.setFacilityImage(BitmapFactory.decodeResource(fragment.getResources(), defaultImageId));
            return;
        }

        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        facility.setFacilityImage(bitmap);
                        saveFacilityImage(bitmap, facility.getFacilityId());
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "IMAGE errorListener " + error.toString());
                        facility.setFacilityImage(BitmapFactory.decodeResource(fragment.getResources(), defaultImageId));
                    }
                });

        // Start loading the image in the background
        RequestQueue requestQueue = getRequestQueue();
        if(requestQueue != null)
            requestQueue.add(request);
    }

    /**
     * Save the Google Maps image of the facility to a file. This file will then be used instead
     * of loading it from Google every time
     * @param bitmap The image of the facility
     * @param facilityId The id of the facility
     */
    public void saveFacilityImage(Bitmap bitmap, int facilityId) {
        File file = getFacilityImageFile(facilityId);
        Utilities.saveBitmapToFile(file, bitmap);
    }

    /**
     * Load the facility image from a file.
     * @param facilityId The id of the facility
     * @return The facility image. Null if the file does not exist
     */
    public Bitmap loadCacheFacilityImage(int facilityId) {
        File file = getFacilityImageFile(facilityId);
        return Utilities.loadBitmapFromFile(file);
    }

    /**
     * Get the file path of the facility image
     * @param facilityId The id of the facility
     * @return The file path of the facility image
     */
    private File getFacilityImageFile(int facilityId) {
        String fileName = "facilityImage" + facilityId;
        return new File(fragment.getActivity().getFilesDir(), fileName);
    }

    /**
     * Get the url for the Street View Api
     * @param address The street address
     * @param town The town
     * @param state The state. Can be initials or full name
     * @return The url for the street view api
     * @throws UnsupportedEncodingException If the address cannot be encoded into a url
     */
    private String calculateStreetViewAPIUrl(String address, String town, String state, int imageWidth, int imageHeight) throws UnsupportedEncodingException {
        String url = "https://maps.googleapis.com/maps/api/streetview?size=" +
                imageWidth + "x" + imageHeight + "&location=";

        // Encode the address
        String params = address + ", " + town + ", " + state;
        return url + URLEncoder.encode(params, "UTF-8");
    }

    /**
     * Get the url for the Google Maps Api
     * @param address The street address
     * @param town The town
     * @param state The state. Can be initials or full name
     * @return The url for the street view api
     * @throws UnsupportedEncodingException If the address cannot be encoded into a url
     */
    private String calculateMapAPIUrl(String address, String town, String state, int imageWidth, int imageHeight) throws UnsupportedEncodingException {
        String url = "http://maps.google.com/maps/api/staticmap?center=";

        // Encode the address
        String paramLocation = address + ", " + town + ", " + state;
        paramLocation = URLEncoder.encode(paramLocation, "UTF-8");

        // Place a red marker over the location
        url += paramLocation + "&zoom=16&size=" + imageWidth + "x" + imageHeight
                + "&sensor=false&markers=color:redzlabel:A%7C" + paramLocation;

        return url;
    }

    /**
     * Determine if the response from the Street View API was a valid street view image
     * If there is no street view imagery for the address, Google returns a static image
     * stating that there is no imagery. I then load a map of the address instead.
     * I cannot find a way to check if the street view exists properly. However, since the image
     * is always the same, I check the color of one of the pixels.
     *
     *** This code will break if Google changes the error message image. ***
     *
     * @param streetViewResponse The bitmap response that the Street View API gave
     * @return Whether the response was a valid street view image
     */
    private boolean validStreetViewBitmap(Bitmap streetViewResponse) {
        int pixel = streetViewResponse.getPixel(100, 100);
        return pixel != -1776674;
    }

    /**
     * Get the request queue and create it if necessary
     * Precondition: FacilitiesFragment is a member of MainActivity
     * @return The request queue
     */
    private RequestQueue getRequestQueue() {
        Activity parentActivity = fragment.getActivity();
        if(parentActivity != null && parentActivity instanceof MainActivity) {
            return ((MainActivity) fragment.getActivity()).getRequestQueue();
        }
        return null;
    }

}
