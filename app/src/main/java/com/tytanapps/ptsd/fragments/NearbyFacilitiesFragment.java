package com.tytanapps.ptsd.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.crash.FirebaseCrash;
import com.tytanapps.ptsd.Facility;
import com.tytanapps.ptsd.FacilityAdapter;
import com.tytanapps.ptsd.MainActivity;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.Utilities;

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

/**
 * Loads a list of nearby VA facilities that offer PTSD programs.
 * Displays the address, phone number, programs, and an image for each facility.
 * You can call them, get directions, or load the website for each VA facility.
 */
public class NearbyFacilitiesFragment extends AnalyticsFragment {

    private static final String LOG_TAG = NearbyFacilitiesFragment.class.getSimpleName();

    private static final int PERMISSION_LOCATION_REQUEST = 3;

    // Stores the facilities that have already loaded
    // Key: VA Id, Value: The facility with the given id
    private HashMap<Integer, Facility> knownFacilities = new HashMap<>();

    // The number of VA facilities that have already loaded, either by API or from cache
    // This number is still incremented when the API load fails
    private int numberOfLoadedFacilities = 0;

    private List<Facility> facilityList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FacilityAdapter mAdapter;

    public NearbyFacilitiesFragment() {
        // Required default constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_nearby_facilities, container, false);

        setupRefreshLayout(rootView);

        return rootView;
    }

    /**
     * When the fragment becomes visible, start loading the VA facilities
     */
    @Override
    public void onStart() {
        super.onStart();
        setupRecyclerView();

        // Load the VA facilities if they have not yet been loaded
        if(knownFacilities.size() == 0) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    loadPTSDPrograms();
                }
            });
            t.run();
        }
    }

    /**
     * Get the root view of the fragment casted to a ViewGroup
     * This is needed when inflating views
     * @return The root view of the fragment as a ViewGroup,
     *         Null if the root view is null or not a ViewGroup
     */
    private ViewGroup getViewGroup() {
        View rootView = getView();
        if(rootView != null && rootView instanceof ViewGroup)
            return (ViewGroup) getView();
        return null;
    }

    /**
     * Setup the RecyclerView and link it to the FacilityAdapter
     */
    private void setupRecyclerView() {
        View rootView = getView();
        if(rootView != null) {
            recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

            mAdapter = new FacilityAdapter(facilityList, getActivity());
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
        }
    }

    /**
     * Setup the refresh layout to refresh on swipe down past the first item
     * @param rootView The root view of the fragment containing the refresh layout
     */
    private void setupRefreshLayout(View rootView) {
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                numberOfLoadedFacilities = 0;
                knownFacilities.clear();
                facilityList.clear();
                mAdapter.notifyDataSetChanged();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadPTSDPrograms();
                    }
                });
                t.run();
            }
        });
        swipeRefreshLayout.setEnabled(false);
    }

    /**
     * Load all PTSD programs and the facility id where they are located.
     * There are multiple PTSD programs per VA facility.
     */
    private void loadPTSDPrograms() {
        // Request the location permission if it has not been granted
        if (!locationPermissionGranted()) {
            requestLocationPermission();
        } else {
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
                            errorLoadingResults();
                            return;
                        }

                        double userLocation[] = Utilities.getGPSLocation(getActivity());
                        // If the user's GPS location cannot be found
                        if (userLocation[0] == 0 && userLocation[1] == 0) {
                            errorLoadingResults(getString(R.string.gps_error));
                            return;
                        }

                        // Add each PTSD program to the correct VA facility
                        for (int i = 1; i < numberOfResults; i++) {
                            JSONObject ptsdProgramJson = rootJson.getJSONObject("" + i);
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
                    errorLoadingResults();
                }
            });

            // Set a longer Volley timeout policy
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            // Start loading the PTSD programs in the background
            RequestQueue requestQueue = getRequestQueue();
            if (requestQueue != null)
                requestQueue.add(stringRequest);
            else
                errorLoadingResults();
        }
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
        String url = calculateFacilityAPIURL(facility.getFacilityId(), getString(R.string.api_key_va_facilities));

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

        // For some reason the facility urls start with vaww. instead of www.
        // These cannot be loaded on my phone so use www. instead.
        String url = (String) locationJson.get("FANDL_URL");
        url = url.replace("vaww", "www");

        double userLocation[] = Utilities.getGPSLocation(getActivity());
        double distance = 0;

        // The description contains the distance and all PTSD programs located there
        if(userLocation[0] != 0 && userLocation[1] != 0) {
            distance = Utilities.distanceBetweenCoordinates(locationLat, locationLong, userLocation[0], userLocation[1], "M");
            facilityToUpdate.setDistance(distance);

            DecimalFormat df = new DecimalFormat("#.##");
            description = "Distance: " + df.format(distance) + " miles\n\n";
        }

        Set<String> programs = facilityToUpdate.getPrograms();
        for(String program : programs)
            description += program + "\n";

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
     * Load the facility image and place it into facilityImageView.
     * Try the street view image first, then the map image, then the default image.
     * @param facility The facility
     */
    private void loadFacilityImage(Facility facility) {
        Bitmap cachedBitmap = loadCacheFacilityImage(facility.getFacilityId());

        if(cachedBitmap != null)
            facility.setFacilityImage(cachedBitmap);
        else {
            int imageWidth = getResources().getInteger(R.integer.map_image_width);
            int imageHeight = getResources().getInteger(R.integer.map_image_height);
            loadStreetViewImage(facility, imageWidth, imageHeight);
        }

        mAdapter.notifyDataSetChanged();
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
            facility.setFacilityImage(BitmapFactory.decodeResource(getResources(), defaultImageId));
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
                        facility.setFacilityImage(BitmapFactory.decodeResource(getResources(), defaultImageId));
                    }
                });

        // Start loading the image in the background
        RequestQueue requestQueue = getRequestQueue();
        if(requestQueue != null)
            requestQueue.add(request);
    }

    /**
     * When all of the facilities have fully loaded, sort them by distance and display them to the user
     */
    private void allFacilitiesHaveLoaded() {
        if(knownFacilities.size() == 0) {
            errorLoadingResults();
            return;
        }

        hideLoadingBar();

        // Sort the facilities by distance
        final ArrayList<Facility> facilities = new ArrayList<>(knownFacilities.values());
        Collections.sort(facilities);

        // Display the facilities
        for(int i = 0; i < getResources().getInteger(R.integer.facilities_to_display); i++) {
            final Facility facility = facilities.get(i);
            facilityList.add(facility);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    loadFacilityImage(facility);
                }
            });
            t.run();
        }

        View rootView = getView();
        if(rootView != null) {
            SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);
        }

        mAdapter.notifyDataSetChanged();
    }

    /**
     * @return Whether the location permission has been granted
     */
    private boolean locationPermissionGranted() {
        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request the location permission
     * Only needed in Android version 6.0 and up
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestLocationPermission() {
        if (!locationPermissionGranted()) {
            requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_LOCATION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    loadPTSDPrograms();

                } else {
                    // Permission denied
                    errorLoadingResults(getString(R.string.error_location_permission));
                }
                return;
            }

            // Other 'case' lines to check for other permissions this app might request
        }
    }

    /**
     * There was an error loading the VA facilities. Display the default error message
     */
    private void errorLoadingResults() {
        errorLoadingResults(getString(R.string.va_loading_error));
    }

    /**
     * There was an error loading the VA facilities. Display a message and a retry button.
     * @param errorMessage The message to show to the user
     */
    private void errorLoadingResults(String errorMessage) {
        View rootView = getView();
        if(rootView != null) {
            final TextView loadingTextview = (TextView) rootView.findViewById(R.id.facility_loading_textview);
            if(loadingTextview != null)
                loadingTextview.setText(errorMessage);

            final ProgressBar loadingProgressbar = (ProgressBar) rootView.findViewById(R.id.facility_progressbar);
            if(loadingProgressbar != null) {
                loadingProgressbar.setVisibility(View.INVISIBLE);
            }

            Button retryButton = (Button) rootView.findViewById(R.id.retry_load_button);
            if(retryButton != null) {
                retryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setVisibility(View.INVISIBLE);

                        if(loadingTextview != null)
                            loadingTextview.setText(R.string.loading);
                        if(loadingProgressbar != null)
                            loadingProgressbar.setVisibility(View.VISIBLE);

                        loadPTSDPrograms();
                    }
                });
                retryButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Remove the loading progress bar and TextView
     */
    private void hideLoadingBar() {
        View rootView = getView();
        if(rootView != null) {
            View loadingTextView = rootView.findViewById(R.id.facility_loading_textview);
            if (loadingTextView != null)
                loadingTextView.setVisibility(View.GONE);

            View loadingProgressbar = rootView.findViewById(R.id.facility_progressbar);
            if (loadingProgressbar != null)
                loadingProgressbar.setVisibility(View.GONE);
        }
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

            double userLocation[] = Utilities.getGPSLocation(getActivity());
            double distance = 0;

            String description = "";

            // The description contains the distance and all PTSD programs located there
            if(userLocation[0] != 0 && userLocation[1] != 0) {
                distance = Utilities.distanceBetweenCoordinates(facility.getLatitude(), facility.getLongitude(), userLocation[0], userLocation[1], "M");
                facility.setDistance(distance);

                DecimalFormat df = new DecimalFormat("#.##");
                description = "Distance: " + df.format(distance) + " miles\n\n";
            }

            Set<String> programs = facility.getPrograms();
            for(String program : programs)
                description += program + "\n";

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
     * Save the Google Maps image of the facility to a file. This file will then be used instead
     * of loading it from Google every time
     * @param bitmap The image of the facility
     * @param facilityId The id of the facility
     */
    private void saveFacilityImage(Bitmap bitmap, int facilityId) {
        File file = getFacilityImageFile(facilityId);
        Utilities.saveBitmapToFile(file, bitmap);
    }

    /**
     * Load the facility image from a file.
     * @param facilityId The id of the facility
     * @return The facility image. Null if the file does not exist
     */
    private Bitmap loadCacheFacilityImage(int facilityId) {
        File file = getFacilityImageFile(facilityId);
        return Utilities.loadBitmapFromFile(file);
    }

    /**
     * Get the file path of the facility
     * @param facilityId The id of the facility
     * @return The file path of the facility
     */
    private File getFacilityFile(int facilityId) {
        String fileName = "facility" + facilityId;
        return new File(getActivity().getFilesDir(), fileName);
    }

    /**
     * Get the file path of the facility image
     * @param facilityId The id of the facility
     * @return The file path of the facility image
     */
    private File getFacilityImageFile(int facilityId) {
        String fileName = "facilityImage" + facilityId;
        return new File(getActivity().getFilesDir(), fileName);
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
                + getString(R.string.api_key_ptsd_programs) + "&ReturnFormat=JSON";
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
     * Get the request queue and create it if necessary
     * Precondition: NearbyFacilitiesFragment is a member of MainActivity
     * @return The request queue
     */
    private RequestQueue getRequestQueue() {
        Activity parentActivity = getActivity();
        if(parentActivity != null && parentActivity instanceof MainActivity) {
            return ((MainActivity) getActivity()).getRequestQueue();
        }
        return null;
    }
    
}
