package com.tytanapps.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.tytanapps.ptsd.Utilities.getRemoteConfigInt;

/**
 * Created by Tyler on 6/26/16.
 */
public abstract class FacilityLoader {
    private static final String LOG_TAG = FacilityLoader.class.getSimpleName();

    private Fragment fragment;

    // The number of VA facilities that have already loaded, either by API or from cache
    // This number is still incremented when the API load fails
    private int numberOfLoadedFacilities = 0;

    // Stores the facilities that have already loaded
    // Key: VA Id, Value: The facility with the given id
    private HashMap<Integer, Facility> knownFacilities = new HashMap<>();

    public FacilityLoader(Fragment fragment) {
        this.fragment = fragment;
    }

    public abstract void errorLoadingResults(String errorMessage);
    public abstract void onSuccess(List<Facility> loadedFacilities);

    /**
     * Load all PTSD programs and the facility id where they are located.
     * There are multiple PTSD programs per VA facility.
     */
    public void loadPTSDPrograms() {
        String url = calculateVaAPIUrl();

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // The JSON that the sever responds starts with //
                // Trim the first two characters to create valid JSON.
                response = response.substring(2);

                // Load the initial JSON request. This this is a program name and the
                // facility ID where it is located.
                try {
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");
                    int numberOfResults = new JSONObject(response).getInt("MATCHES");

                    if (numberOfResults == 0) {
                        errorLoadingResults("Error");
                        return;
                    }

                    double userLocation[] = Utilities.getGPSLocation(fragment.getActivity());
                    // If the user's GPS location cannot be found
                    if (userLocation[0] == 0 && userLocation[1] == 0) {
                        errorLoadingResults(fragment.getString(R.string.gps_error));
                        return;
                    }

                    // Add each PTSD program to the correct VA facility
                    for (int i = 1; i < numberOfResults; i++) {
                        JSONObject ptsdProgramJson = rootJson.getJSONObject(""+i);
                        addPTSDProgram(ptsdProgramJson);
                    }
                } catch (JSONException e) {
                    FirebaseCrash.report(e);
                    e.printStackTrace();
                }

                // We only have the id of each facility. Load the rest of the information
                // about that location such as phone number and address.
                if (knownFacilities != null && knownFacilities.size() > 0) {
                    for (int facilityId : knownFacilities.keySet()) {
                        Facility facility = knownFacilities.get(facilityId);

                        // Try to load the facility from cache
                        Facility cachedFacility = readCachedFacility(facilityId);
                        if (cachedFacility != null) {
                            facility = cachedFacility;
                            knownFacilities.put(facilityId, facility);
                            numberOfLoadedFacilities++;

                            // When all facilities have loaded, sort them by distance and show them to the user
                            if (numberOfLoadedFacilities == knownFacilities.size())
                                allFacilitiesHaveLoaded();
                        }
                        // Load the facility using the api. Display them after they all load
                        else
                            loadFacility(facility, knownFacilities.size());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error.toString());
                errorLoadingResults("Error");
            }
        });

        // Set a longer Volley timeout policy
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, // Timeout in milliseconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Start loading the PTSD programs in the background
        RequestQueue requestQueue = getRequestQueue();
        if (requestQueue != null)
            requestQueue.add(stringRequest);
        else
            errorLoadingResults("Error");
    }

    /**
     * Add a PTSD program to the VA facility in which its held
     * @param ptsdProgramJson The JSON representing the PTSD program
     * @throws JSONException Invalid JSON for the PTSD program
     */
    private void addPTSDProgram(JSONObject ptsdProgramJson) throws JSONException {
        int facilityID = ptsdProgramJson.getInt("FAC_ID");
        String programName = (String) ptsdProgramJson.get("PROGRAM");

        // There are multiple programs at the same facility.
        // Combine them if necessary.
        Facility facility;
        if (knownFacilities.containsKey(facilityID))
            facility = knownFacilities.get(facilityID);
        else
            facility = new Facility(facilityID);

        facility.addProgram(programName);
        knownFacilities.put(facilityID, facility);
    }

    /**
     * Fully load a single facility
     * @param facility The facility to load, must contain an id. The results of the load are placed into it
     * @param numberOfFacilities The number of facilities that are being loaded
     */
    private void loadFacility(final Facility facility, final int numberOfFacilities) {
        String url = calculateFacilityAPIURL(facility.getFacilityId(), fragment.getString(R.string.api_key_va_facilities));

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // The JSON that the sever responds starts with //
                // I am cropping the first two characters to create valid JSON.
                response = response.substring(2);

                try {
                    // Get all of the information about the facility
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");
                    parseJSONFacility(facility, rootJson);

                    // Save the facility to a file so it doesn't need to be loaded next time
                    cacheFacility(facility);

                    numberOfLoadedFacilities++;

                    // When all facilities have loaded, sort them by distance and show them to the user
                    if(numberOfLoadedFacilities == numberOfFacilities)
                        allFacilitiesHaveLoaded();

                } catch (JSONException e) {
                    FirebaseCrash.report(e);
                    e.printStackTrace();
                    numberOfLoadedFacilities++;

                    // When all facilities have loaded, sort them by distance and show them to the user
                    if(numberOfLoadedFacilities == numberOfFacilities)
                        allFacilitiesHaveLoaded();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error.toString());
                numberOfLoadedFacilities++;

                // When all facilities have loaded, sort them by distance and show them to the user
                if(numberOfLoadedFacilities == numberOfFacilities)
                    allFacilitiesHaveLoaded();
            }
        });

        // Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions.
        // Volley does retry for you if you have specified the policy.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Start loading the image in the background
        RequestQueue requestQueue = getRequestQueue();
        if(requestQueue != null)
            requestQueue.add(stringRequest);
        else
            numberOfLoadedFacilities++;
    }

    /**
     * Parse the JSON facility and save it to a Facility object
     * @param facilityToUpdate The facility to save the information to
     * @param rootJson To JSON containing information about the VA facility
     * @throws JSONException Invalid facility JSON
     */
    private Facility parseJSONFacility(Facility facilityToUpdate, JSONObject rootJson) throws JSONException {
        JSONObject locationJson = rootJson.getJSONObject("1");

        String name = (String) locationJson.get("FAC_NAME");
        String phoneNumber = (String) locationJson.get("PHONE_NUMBER");
        String address = (String) locationJson.get("ADDRESS");
        String city = (String) locationJson.get("CITY");
        String state = (String) locationJson.get("STATE");
        String zip = ""+locationJson.get("ZIP");
        double locationLat = locationJson.getDouble("LATITUDE");
        double locationLong = locationJson.getDouble("LONGITUDE");
        String description = "";
        String url = getFacilityUrl(locationJson);


        double userLocation[] = Utilities.getGPSLocation(fragment.getActivity());
        // The description contains the distance and all PTSD programs located there
        if(userLocation[0] != 0 && userLocation[1] != 0) {
            double distance = Utilities.distanceBetweenCoordinates(locationLat, locationLong, userLocation[0], userLocation[1], "M");
            facilityToUpdate.setDistance(distance);

            DecimalFormat df = new DecimalFormat("#.##");
            description = "Distance: " + df.format(distance) + " miles\n";
        }

        Set<String> programs = facilityToUpdate.getPrograms();
        for(String program : programs)
            description += "\n" + program;

        facilityToUpdate.setName(name);
        facilityToUpdate.setPhoneNumber(Utilities.getFirstPhoneNumber(phoneNumber));
        facilityToUpdate.setUrl(url);
        facilityToUpdate.setAddress(address, city, state, zip);
        facilityToUpdate.setDescription(description);
        facilityToUpdate.setLatitude(locationLat);
        facilityToUpdate.setLongitude(locationLong);

        return facilityToUpdate;
    }

    /**
     * Get the website of the VA facility
     * @param locationJson The JSON object representing the facility
     * @return The url of the facility
     * @throws JSONException The facility json is not valid
     */
    private String getFacilityUrl(JSONObject locationJson) throws JSONException {
        // For some reason the facility urls start with vaww. instead of www.
        // These cannot be loaded on my phone so use www. instead.
        String url = (String) locationJson.get("FANDL_URL");
        url = url.replace("vaww", "www");
        return url;
    }

    public void loadFacilityImage(Facility facility) {
        loadFacilityImage(facility, new Runnable() {
            @Override
            public void run() {}
        });
    }

    /**
     * Load the facility image and place it into facilityImageView.
     * Try the street view image first, then the map image, then the default image.
     * @param facility The facility
     */
    public void loadFacilityImage(Facility facility, Runnable callback) {
        Bitmap cachedBitmap = loadCacheFacilityImage(facility.getFacilityId());

        if(cachedBitmap != null) {
            facility.setFacilityImage(cachedBitmap);
            callback.run();
        }
        else {
            int imageWidth = getRemoteConfigInt(fragment, R.string.rc_map_width);
            int imageHeight = getRemoteConfigInt(fragment, R.string.rc_map_height);

            loadStreetViewImage(facility, imageWidth, imageHeight, callback);
        }
    }

    /**
     * Load the street view imagery for the given address.
     * If there is no street view imagery, it uses the map view instead.
     * You should not call this directly. Call loadFacilityImage instead
     * @param facility The facility
     */
    private void loadStreetViewImage(final Facility facility, final int imageWidth, final int imageHeight, final Runnable callback) {
        String url = "";

        // If the street view url cannot be created, load the map view instead
        try {
            url = calculateStreetViewAPIUrl(facility.getStreetAddress(), facility.getCity(), facility.getState(), imageWidth, imageHeight);
        } catch (UnsupportedEncodingException e) {
            FirebaseCrash.report(e);
            loadMapImage(facility, imageWidth, imageHeight, callback);
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
                            callback.run();
                        }
                        else
                            loadMapImage(facility, imageWidth, imageHeight, callback);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "Street View Image errorListener: " + error.toString());

                        // Load the map view instead
                        loadMapImage(facility, imageWidth, imageHeight, callback);
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
    private void loadMapImage(final Facility facility, int imageWidth, int imageHeight, final Runnable callback) {
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
                        callback.run();
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "IMAGE errorListener " + error.toString());
                        facility.setFacilityImage(BitmapFactory.decodeResource(fragment.getResources(), defaultImageId));
                        callback.run();
                    }
                });

        // Start loading the image in the background
        RequestQueue requestQueue = getRequestQueue();
        if(requestQueue != null)
            requestQueue.add(request);
    }

    /**
     * Called when all facilities have loaded and knownValues is fully populated
     */
    public void allFacilitiesHaveLoaded() {
        ArrayList<Facility> facilitiesList = new ArrayList<>();
        for(Facility facility : knownFacilities.values()) {
            facilitiesList.add(facility);
        }

        // Sort the facilities by distance
        Collections.sort(facilitiesList);

        onSuccess(facilitiesList);
    };

    public void refresh() {
        numberOfLoadedFacilities = 0;
        knownFacilities.clear();
        loadPTSDPrograms();
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
     * Save the facility to a file instead of loading every time
     * @param facility The facility to cache
     */
    private void cacheFacility(Facility facility) {
        File file = getFacilityFile(facility.getFacilityId());
        ObjectOutput out;

        try {
            out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(facility);
            out.close();
        } catch (IOException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }
    }

    /**
     * Load the facility from the saved file
     * @param facilityId The id of the facility
     * @return The facility with the given id. Null if the facility is not saved
     */
    private Facility readCachedFacility(int facilityId) {
        ObjectInputStream input;
        File file = getFacilityFile(facilityId);

        Facility facility = null;

        try {
            input = new ObjectInputStream(new FileInputStream(file));
            facility = (Facility) input.readObject();

            double userLocation[] = Utilities.getGPSLocation(fragment.getActivity());
            double distance = 0;

            String description = "";

            // The description contains the distance and all PTSD programs located there
            if(userLocation[0] != 0 && userLocation[1] != 0) {
                distance = Utilities.distanceBetweenCoordinates(facility.getLatitude(), facility.getLongitude(), userLocation[0], userLocation[1], "M");
                facility.setDistance(distance);

                DecimalFormat df = new DecimalFormat("#.##");
                description = "Distance: " + df.format(distance) + " miles\n";
            }

            Set<String> programs = facility.getPrograms();
            for(String program : programs)
                description += "\n" + program;

            facility.setDescription(description);

            input.close();
        } catch (FileNotFoundException e) {
            // If the file was not found, nothing is wrong.
            // It just means that the facility has not yet been cached.
        } catch (IOException | ClassNotFoundException e) {
            FirebaseCrash.report(e);
            e.printStackTrace();
        }

        return facility;
    }

    /**
     * Get the file path of the facility
     * @param facilityId The id of the facility
     * @return The file path of the facility
     */
    private File getFacilityFile(int facilityId) {
        String fileName = "facility" + facilityId;
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
     * Get the url for the PTSD Programs API
     * @return The url for the PTSD Programs API
     */
    private String calculateVaAPIUrl() {
        return "http://www.va.gov/webservices/PTSD/ptsd.cfc?method=PTSD_Program_Locator_array&license="
                + fragment.getString(R.string.api_key_ptsd_programs) + "&ReturnFormat=JSON";
    }

    /**
     * Get the url for the VA facility API
     * @param facilityId The id of the facility to load
     * @param licenceKey The API licence key
     * @return The url for the VA facility API
     */
    private String calculateFacilityAPIURL(int facilityId, String licenceKey) {
        return "http://www.va.gov/webservices/fandl/facilities.cfc?method=GetFacsDetailByFacID_array&fac_id="
                + facilityId + "&license=" + licenceKey + "&ReturnFormat=JSON";
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
