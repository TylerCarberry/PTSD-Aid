package me.tylercarberry.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NearbyFacilitiesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NearbyFacilitiesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String LOG_TAG = NearbyFacilitiesFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private RequestQueue requestQueue;

    // Dimensions for the Google Maps ImageView
    private static final int MAP_IMAGEVIEW_WIDTH = 640; // You cannot exceed 640 in the free tier
    private static final int MAP_IMAGEVIEW_HEIGHT = 400;

    private HashMap<Integer, Facility> knownFacilities = new HashMap<>();

    private int numberOfLoadedFacilities = 0;

    // The number of facilities to display on screen
    private static final int FACILITIES_TO_SHOW = 20;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NearbyFacilitiesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NearbyFacilitiesFragment newInstance(String param1, String param2) {
        NearbyFacilitiesFragment fragment = new NearbyFacilitiesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public NearbyFacilitiesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nearby_facilities, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadPTSDPrograms();
    }

    /**
     * Load all PTSD programs and the facility id where they are located. There are multiple
     * PTSD programs per facility
     */
    public void loadPTSDPrograms() {
        Toast.makeText(getActivity(), "LOADING... This may take a while", Toast.LENGTH_LONG).show();

        String url = "http://www.va.gov/webservices/PTSD/ptsd.cfc?method=PTSD_Program_Locator_array&license="
                + getString(R.string.api_key_ptsd_programs) + "&ReturnFormat=JSON";

        if(requestQueue == null)
            instantiateRequestQueue();

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(LOG_TAG, response);

                // The JSON that the sever responds starts with //
                // I am cropping the first two characters to create valid JSON.
                response = response.substring(2);

                // Load the initial JSON request. This this is a program name and the
                // facility ID where it is located.
                try {
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");
                    int numberOfResults = new JSONObject(response).getInt("MATCHES");

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
                    for (int facilityId : knownFacilities.keySet()) {
                        Facility facility = knownFacilities.get(facilityId);
                        loadFacility(facility, knownFacilities.size());
                    }
                }

                Toast.makeText(getActivity(), "Known facilities: " + knownFacilities.size(), Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error.toString());
            }
        });

        // Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions.
        // Volley does retry for you if you have specified the policy.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);

    }

    /**
     * Fully load a single facility
     * @param facility The facility to load, must contain an id. The results of the load are placed into it
     * @param numberOfFacilities The number of facilities that are being loaded
     */
    public void loadFacility(final Facility facility, final int numberOfFacilities) {
        String url = "http://www.va.gov/webservices/fandl/facilities.cfc?method=GetFacsDetailByFacID_array&fac_id="
                + facility.getFacilityId() + "&license=" + getString(R.string.api_key_va_facilities) + "&ReturnFormat=JSON";

        if(requestQueue == null)
            instantiateRequestQueue();

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


                    String description = "Desc";


                    String url = (String) locationJson.get("FANDL_URL");
                    // For some reason the facility urls start with vaww. instead of www.
                    // These cannot be loaded on my phone so use www. instead.
                    url = url.replace("vaww", "www");


                    double userLocation[] = getGPSLocation();
                    double distance = 0;

                    // The description contains the distance and all PTSD programs located there
                    if(userLocation[0] != 0 && userLocation[1] != 0) {
                        distance = distanceBetweenCoordinates(locationLat, locationLong, userLocation[0], userLocation[1], "M");
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

                    numberOfLoadedFacilities++;

                    Log.d(LOG_TAG, "Progress: " + numberOfLoadedFacilities + " / " + numberOfFacilities);

                    // When all facilities have loaded, sort them by distance and show them to the user
                    if(numberOfLoadedFacilities == numberOfFacilities)
                        allFacilitiesHaveLoaded();

                } catch (JSONException e) {
                    e.printStackTrace();
                    numberOfLoadedFacilities++;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, error.toString());
                numberOfLoadedFacilities++;
            }
        });

        // Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions.
        // Volley does retry for you if you have specified the policy.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Start loading the facility in the background
        requestQueue.add(stringRequest);
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
     * @throws UnsupportedEncodingException
     */
    private void loadStreetViewImage(final ImageView imageView, final Facility facility) {
        Log.d(LOG_TAG, "Entering load street view image.");

        String url = "";

        // If the street view url cannot be created, load the map view instead
        try {
            url = calculateStreetViewUrl(facility.getStreetAddress(), facility.getCity(), facility.getState());
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

        if(requestQueue == null)
            instantiateRequestQueue();

        // Start loading the image in the background
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
        //Log.d(LOG_TAG, "Entering load map image.");

        final int defaultImageId = R.drawable.nspl;

        String url = null;
        try {
            url = calculateMapUrl(facility.getStreetAddress(), facility.getCity(), facility.getState());
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
                        //Log.d(LOG_TAG, "IMAGE onResponse");

                        imageView.setImageBitmap(bitmap);
                        saveFacilityImage(bitmap, facility.getFacilityId());
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "IMAGE errorListener");
                        imageView.setImageResource(defaultImageId);
                    }
                });

        if(requestQueue == null)
            instantiateRequestQueue();

        // Start loading the image in the background
        requestQueue.add(request);
    }

    /**
     * When all of the facilities have fully loaded, sort them by distance and display them to the user
     */
    private void allFacilitiesHaveLoaded() {
        ArrayList<Facility> facilities = new ArrayList<>(knownFacilities.values());

        // Sort the facilities by distance
        Collections.sort(facilities);

        for(int i = 0; i < FACILITIES_TO_SHOW; i++)
            addFacilityCard(facilities.get(i));

    }

    /**
     * Save the Google Maps image of the facility to a file. This file will then be used instead
     * of loading it from Google every time
     * @param bitmap The image of the facility
     * @param facilityId The id of the facility
     */
    private void saveFacilityImage(Bitmap bitmap, int facilityId) {
        File file = getFacilityImageFile(facilityId);

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
     * Load the facility image from a file.
     * @param facilityId The id of the facility
     * @return The facility image. Null if the file does not exist
     */
    private Bitmap loadCacheFacilityImage(int facilityId) {
        File file = getFacilityImageFile(facilityId);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return bitmap;
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
     * Create the request queue. This is used to connect to the API in the background
     */
    private void instantiateRequestQueue() {
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getActivity().getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();
    }


    /**
     * Determine if the response from the Street View API was a valid street view image
     *
     * If there is no street view imagery for the address, Google returns an image
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
    private String calculateStreetViewUrl(String address, String town, String state) throws UnsupportedEncodingException {
        String url = "https://maps.googleapis.com/maps/api/streetview?size="+MAP_IMAGEVIEW_WIDTH+"x"+MAP_IMAGEVIEW_HEIGHT+"&location=";

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
    private String calculateMapUrl(String address, String town, String state) throws UnsupportedEncodingException {
        String url = "http://maps.google.com/maps/api/staticmap?center=";

        // Encode the address
        String paramLocation = address + ", " + town + ", " + state;
        paramLocation = URLEncoder.encode(paramLocation, "UTF-8");

        // Place a red marker over the location
        url += paramLocation + "&zoom=16&size=" + MAP_IMAGEVIEW_WIDTH + "x" + MAP_IMAGEVIEW_HEIGHT
                + "&sensor=false&markers=color:redzlabel:A%7C" + paramLocation;

        return url;
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
            RelativeLayout cardRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.facility_layout, null, false);

            TextView nameTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_name_textview);
            nameTextView.setText(facility.getName());

            String description = facility.getDescription();

            TextView descriptionTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_details);
            descriptionTextView.setText(description);

            View.OnClickListener callOnClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String phoneNumber = getFirstPhoneNumber(facility.getPhoneNumber());
                    openDialer(phoneNumber);
                }
            };


            TextView phoneTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_phone_textview);
            phoneTextView.setText(facility.getPhoneNumber());
            phoneTextView.setOnClickListener(callOnClick);

            ImageView phoneIcon = (ImageView) cardRelativeLayout.findViewById(R.id.facility_phone_icon);
            phoneIcon.setOnClickListener(callOnClick);

            ImageView facilityImageView = (ImageView) cardRelativeLayout.findViewById(R.id.facility_imageview);
            loadFacilityImage(facilityImageView, facility);
            facilityImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        openMapIntent(getMapUri(facility.getName(), facility.getCity(), facility.getState()));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });

            View.OnClickListener websiteOnClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = getFirstPhoneNumber(facility.getUrl());
                    openUrl(url);
                }
            };

            ImageView webIcon = (ImageView) cardRelativeLayout.findViewById(R.id.facility_website_icon);
            webIcon.setOnClickListener(websiteOnClick);

            TextView webTextView = (TextView) cardRelativeLayout.findViewById(R.id.website_textview);
            webTextView.setText(facility.getUrl());
            webTextView.setOnClickListener(websiteOnClick);

            LinearLayout parentLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.facilities_linear_layout);
            parentLinearLayout.addView(cardRelativeLayout);
        }
    }

    /**
     * Open the maps app to a specified location
     * @param geoLocation The uri of the location to open
     */
    public void openMapIntent(Uri geoLocation) {
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
     * Determine the first phone number in the String
     * The Facility API returns multiple phone numbers with an or between them
     * @param phoneNumbers A string of one or more phone numbers
     * @return The first phone number in the string
     */
    private String getFirstPhoneNumber(String phoneNumbers) {
        int orLocation = phoneNumbers.indexOf(" Or");

        if(orLocation >= 0)
            return phoneNumbers.substring(0, orLocation);
        return phoneNumbers;
    }

    /**
     * Get the user's GPS location
     * @return The GPS coordinates: latitude, longitude
     */
    private double[] getGPSLocation() {
        double[] gps = new double[2];

        LocationManager lm = (LocationManager) getActivity().getSystemService(
                Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        Location l = null;

        for (int i = providers.size()-1; i >= 0; i--) {
            try {
                l = lm.getLastKnownLocation(providers.get(i));
            } catch (SecurityException e) {
                // The user has blocked location
            }
            if (l != null) break;
        }

        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;
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
    private double distanceBetweenCoordinates(double lat1, double lon1, double lat2, double lon2, String unit) {
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

    private static double degreesToRadians(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double radiansToDegrees(double rad) {
        return (rad * 180 / Math.PI);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
