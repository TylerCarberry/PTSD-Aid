package com.tytanapps.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

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
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * Loads a list of nearby VA facilities that offer PTSD programs.
 * Displays the address, phone number, programs, and an image for each facility.
 * You can call them, get directions, or load the website for each VA facility.
 */
public class NearbyFacilitiesFragment extends Fragment {

    private static final String LOG_TAG = NearbyFacilitiesFragment.class.getSimpleName();

    // Dimensions for the Google Maps ImageView
    private static final int MAP_IMAGE_WIDTH = 640; // You cannot exceed 640 in the free tier
    private static final int MAP_IMAGE_HEIGHT = 400;

    // Stores the facilities that have already loaded
    // Key: VA Id, Value: The facility with the given id
    private HashMap<Integer, Facility> knownFacilities = new HashMap<>();

    // The number of VA facilities that have already loaded, either by API or from cache
    // This number is still incremented when the API load fails
    private int numberOfLoadedFacilities = 0;

    // The number of facilities to display on screen
    private static final int FACILITIES_TO_DISPLAY = 15;

    // Required default constructor
    public NearbyFacilitiesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nearby_facilities, container, false);
    }

    /**
     * Get the root view of the fragment casted to a ViewGroup
     * This is needed when inflating views
     * @return The root view of the fragment as a ViewGroup,
     *         Null if the root view is null or not a ViewGroup
     */
    private ViewGroup getViewGroup() {
        View rootView = getView();
        if(rootView instanceof ViewGroup)
            return (ViewGroup) getView();
        return null;
    }

    /**
     * When the fragment becomes visible, start loading the VA facilities
     */
    @Override
    public void onStart() {
        super.onStart();

        // Prevent loading the facilities multiple times
        if(knownFacilities.size() == 0)
            loadPTSDPrograms();
    }

    /**
     * Load all PTSD programs and the facility id where they are located.
     * There are multiple PTSD programs per VA facility.
     */
    private void loadPTSDPrograms() {
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

                    if(numberOfResults == 0) {
                        errorLoadingResults();
                        return;
                    }

                    double userLocation[] = Utilities.getGPSLocation(getActivity());
                    // If the user's GPS location cannot be found
                    if(userLocation[0] == 0 && userLocation[1] == 0) {
                        errorLoadingResults("Your GPS location cannot be determined");
                        return;
                    }

                    // Add each PTSD program to the correct VA facility
                    for(int i = 1; i < numberOfResults; i++) {
                        JSONObject ptsdProgramJson = rootJson.getJSONObject(""+i);

                        int facilityID = ptsdProgramJson.getInt("FAC_ID");
                        String programName = (String) ptsdProgramJson.get("PROGRAM");

                        // There are multiple programs at the same facility.
                        // Combine them if necessary.
                        Facility facility;
                        if(knownFacilities.containsKey(facilityID))
                            facility = knownFacilities.get(facilityID);
                        else
                            facility = new Facility(facilityID);

                        facility.addProgram(programName);
                        knownFacilities.put(facilityID, facility);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // We only have the id of each facility. Load the rest of the information
                // about that location such as phone number and address.
                if(knownFacilities != null && knownFacilities.size() > 0) {
                    for(int facilityId : knownFacilities.keySet()) {
                        Facility facility = knownFacilities.get(facilityId);

                        // Try to load the facility from cache
                        Facility cachedFacility = readCachedFacility(facilityId);
                        if(cachedFacility != null) {
                            facility = cachedFacility;
                            knownFacilities.put(facilityId, facility);
                            numberOfLoadedFacilities++;

                            // When all facilities have loaded, sort them by distance and show them to the user
                            if(numberOfLoadedFacilities == knownFacilities.size())
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
        if(requestQueue != null)
            requestQueue.add(stringRequest);
        else
            errorLoadingResults();
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
                Log.d(LOG_TAG, response);

                // The JSON that the sever responds starts with //
                // I am cropping the first two characters to create valid JSON.
                response = response.substring(2);

                try {
                    // Get all of the information about the facility
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");
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
                        facility.setDistance(distance);

                        DecimalFormat df = new DecimalFormat("#.##");
                        description = "Distance: " + df.format(distance) + " miles\n\n";
                    }

                    Set<String> programs = facility.getPrograms();
                    for(String program : programs)
                        description += program + "\n";

                    facility.setName(name);
                    facility.setPhoneNumber(phoneNumber);
                    facility.setUrl(url);
                    facility.setStreetAddress(address);
                    facility.setCity(city);
                    facility.setState(state);
                    facility.setZip(zip);
                    facility.setDescription(description);
                    facility.setLatitude(locationLat);
                    facility.setLongitude(locationLong);

                    // Save the facility to a file so it doesn't need to be loaded next time
                    cacheFacility(facility);

                    numberOfLoadedFacilities++;

                    // When all facilities have loaded, sort them by distance and show them to the user
                    if(numberOfLoadedFacilities == numberOfFacilities)
                        allFacilitiesHaveLoaded();

                } catch (JSONException e) {
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
     * Load the facility image and place it into facilityImageView.
     * Try the street view image first, then the map image, then the default image.
     * @param facilityImageView The ImageView to place the image into
     * @param facility The facility
     */
    private void loadFacilityImage(ImageView facilityImageView, Facility facility) {
        Bitmap cachedBitmap = loadCacheFacilityImage(facility.getFacilityId());

        if(cachedBitmap != null)
            facilityImageView.setImageBitmap(cachedBitmap);
        else
            loadStreetViewImage(facilityImageView, facility);
    }

    /**
     * Load the street view imagery for the given address.
     * If there is no street view imagery, it uses the map view instead.
     * You should not call this directly. Call loadFacilityImage instead
     * @param imageView The ImageView to place the image into
     * @param facility The facility
     */
    private void loadStreetViewImage(final ImageView imageView, final Facility facility) {
        Log.d(LOG_TAG, "Entering load street view image.");

        String url = "";

        // If the street view url cannot be created, load the map view instead
        try {
            url = calculateStreetViewAPIUrl(facility.getStreetAddress(), facility.getCity(), facility.getState());
        } catch (UnsupportedEncodingException e) {
            loadMapImage(imageView, facility);
            return;
        }
        Log.d(LOG_TAG, url);

        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        // If there is no street view image for the address use the map view instead
                        if(validStreetViewBitmap(bitmap)) {
                            imageView.setImageBitmap(bitmap);
                            saveFacilityImage(bitmap, facility.getFacilityId());
                        }
                        else
                            loadMapImage(imageView, facility);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "Street View Image errorListener: " + error.toString());

                        // Load the map view instead
                        loadMapImage(imageView, facility);
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
     * @param imageView The ImageView to place the image into
     * @param facility The facility
     */
    private void loadMapImage(final ImageView imageView, final Facility facility) {
        //Log.d(LOG_TAG, "loadMapImage() called with: " + "imageView = [" + imageView + "], facility = [" + facility + "]");

        final int defaultImageId = R.drawable.default_facility_image;

        String url;
        try {
            url = calculateMapAPIUrl(facility.getStreetAddress(), facility.getCity(), facility.getState());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            imageView.setImageResource(defaultImageId);
            return;
        }
        Log.d(LOG_TAG, url);


        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        imageView.setImageBitmap(bitmap);
                        saveFacilityImage(bitmap, facility.getFacilityId());
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "IMAGE errorListener " + error.toString());
                        imageView.setImageResource(defaultImageId);
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
        ArrayList<Facility> facilities = new ArrayList<>(knownFacilities.values());
        Collections.sort(facilities);

        // Display the facilities
        for(int i = 0; i < FACILITIES_TO_DISPLAY; i++)
            addFacilityCard(facilities.get(i));
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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
     *
     * If there is no street view imagery for the address, Google returns a static image
     * stating that there is no imagery. I then load a map of the address instead.
     *
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
    private String calculateStreetViewAPIUrl(String address, String town, String state) throws UnsupportedEncodingException {
        String url = "https://maps.googleapis.com/maps/api/streetview?size=" +
                MAP_IMAGE_WIDTH + "x" + MAP_IMAGE_HEIGHT + "&location=";

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
    private String calculateMapAPIUrl(String address, String town, String state) throws UnsupportedEncodingException {
        String url = "http://maps.google.com/maps/api/staticmap?center=";

        // Encode the address
        String paramLocation = address + ", " + town + ", " + state;
        paramLocation = URLEncoder.encode(paramLocation, "UTF-8");

        // Place a red marker over the location
        url += paramLocation + "&zoom=16&size=" + MAP_IMAGE_WIDTH + "x" + MAP_IMAGE_HEIGHT
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
     * Get the url for the Google Maps Api
     * @param name The street address
     * @param town The town
     * @param state The state. Can be initials or full name
     * @return The url for the street view api
     * @throws UnsupportedEncodingException If the address cannot be encoded into a url
     */
    private Uri getMapUri(String name, String town, String state) throws UnsupportedEncodingException {
        // Encode the address
        String location = name + ", " + town + ", " + state;
        location = URLEncoder.encode(location, "UTF-8");

        return Uri.parse("geo:0,0?q=" + location);
    }

    /**
     * Add a card to the list containing information about the facility
     * @param facility The facility to add
     */
    private void addFacilityCard(final Facility facility) {
        View fragmentView = getView();
        if(fragmentView != null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            RelativeLayout cardRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.facility_layout, getViewGroup(), false);

            // Get the information from the facility
            String name = facility.getName();
            String description = facility.getDescription();
            final String phoneNumber = Utilities.getFirstPhoneNumber(facility.getPhoneNumber());

            // If the facility does not have all of its information, do not show it
            if(name != null && description != null && phoneNumber != null) {
                TextView nameTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_name_textview);
                nameTextView.setText(name);

                TextView descriptionTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_details);
                descriptionTextView.setText(description);

                View.OnClickListener callOnClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDialer(phoneNumber);
                    }
                };

                TextView phoneTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_phone_textview);
                phoneTextView.setText(phoneNumber);
                phoneTextView.setOnClickListener(callOnClick);

                ImageView phoneIcon = (ImageView) cardRelativeLayout.findViewById(R.id.facility_phone_icon);
                phoneIcon.setOnClickListener(callOnClick);

                View.OnClickListener mapOnClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            openMapIntent(getMapUri(facility.getName(), facility.getCity(), facility.getState()));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                };

                TextView addressTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_address_textview);
                addressTextView.setText(facility.getFullAddress());
                addressTextView.setOnClickListener(mapOnClick);

                ImageView addressIcon = (ImageView) cardRelativeLayout.findViewById(R.id.facility_address_icon);
                addressIcon.setOnClickListener(mapOnClick);

                ImageView facilityImageView = (ImageView) cardRelativeLayout.findViewById(R.id.facility_imageview);
                loadFacilityImage(facilityImageView, facility);
                facilityImageView.setOnClickListener(mapOnClick);

                // Tapping the more info button opens the website
                Button moreInfoButton = (Button) cardRelativeLayout.findViewById(R.id.more_info_button);
                moreInfoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = Utilities.getFirstPhoneNumber(facility.getUrl());
                        openUrl(url);
                    }
                });

                // Add the facility card to the list
                LinearLayout parentLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.facilities_linear_layout);
                parentLinearLayout.addView(cardRelativeLayout);
            }
        }
    }

    /**
     * Open the maps app to a specified location
     * @param geoLocation The uri of the location to open
     */
    private void openMapIntent(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Open the dialer with a phone number entered
     * This does not call the number directly, the user needs to press the call button
     * @param phoneNumber The phone number to call
     */
    private void openDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    /**
     * Opens the browser to the specified url
     * Precondition: url is a valid url
     * @param url The url to open
     */
    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    /**
     * Get the request queue and create it if necessary
     * Precondition: NearbyFacilitiesFragment is a member of MainActivity
     * @return The request queue
     */
    private RequestQueue getRequestQueue() {
        Activity parentActivity = getActivity();
        if(parentActivity != null && parentActivity instanceof MainActivity) {
            RequestQueue requestQueue = ((MainActivity) getActivity()).getRequestQueue();
            if (requestQueue == null) {
                ((MainActivity) getActivity()).instantiateRequestQueue();
                requestQueue = ((MainActivity) getActivity()).getRequestQueue();
            }
            return requestQueue;
        }
        return null;
    }

}
