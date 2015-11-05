package me.tylercarberry.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.List;

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
        loadNearbyPTSDPrograms();

        //loadNearbyFacilities();
    }


    public void loadNearbyPTSDPrograms() {

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

                try {
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");

                    for(int i = 1; i < 30; i++) {
                        JSONObject locationJson = rootJson.getJSONObject("" + i);

                        int facilityID = locationJson.getInt("FAC_ID");

                        String program = (String) locationJson.get("PROGRAM");

                        loadFacility(facilityID, program);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
     * Load nearby VA facilities from the VA API.
     * TODO: Load facilities near the user's location
     */
    public void loadFacility(int facilityId, final String program) {
        //Toast.makeText(getActivity(), "LOADING... This may take a while", Toast.LENGTH_LONG).show();

        String url = "http://www.va.gov/webservices/fandl/facilities.cfc?method=GetFacsDetailByFacID_array&fac_id="
                + facilityId + "&license=" + getString(R.string.api_key_va_facilities) + "&ReturnFormat=JSON";

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
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");
                    JSONObject locationJson = rootJson.getJSONObject("1");

                    String name = (String) locationJson.get("FAC_NAME");
                    Log.d(LOG_TAG, name);

                    String description = "Desc";

                    String phoneNumber = (String) locationJson.get("PHONE_NUMBER");
                    String address = (String) locationJson.get("ADDRESS");
                    String city = (String) locationJson.get("CITY");
                    String state = (String) locationJson.get("STATE");
                    String zip = ""+locationJson.get("ZIP");
                    //String zip = "";

                    double locationLat = locationJson.getDouble("LATITUDE");
                    double locationLong = locationJson.getDouble("LONGITUDE");

                    int facilityID = locationJson.getInt("FAC_ID");

                    Facility facility = new Facility(facilityID, name, phoneNumber, address, city, state, zip, locationLat, locationLong);

                    double userLocation[] = getGPSLocation();
                    double distance = 0;

                    if(userLocation[0] != 0 && userLocation[1] != 0) {
                        distance = distanceBetweenCoordinates(locationLat, locationLong, userLocation[0], userLocation[1], "M");

                        DecimalFormat df = new DecimalFormat("#.##");
                        description = "Distance: " + df.format(distance) + " miles";
                    }

                    addFacilityCard(name, program + "\n" + description, phoneNumber, address, city, state);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
     * Load nearby VA facilities from the VA API.
     * TODO: Load facilities near the user's location
     */
    public void loadNearbyFacilities() {
        Toast.makeText(getActivity(), "LOADING... This may take a while", Toast.LENGTH_LONG).show();

        String url = "http://www.va.gov/webservices/fandl/facilities.cfc?method=Facility_byRegionIDandType_detail_array&fac_fld=NJ&fac_val=5,7&license="
                + getString(R.string.api_key_va_facilities)
                + "&ReturnFormat=JSON";

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
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");

                    for(int i = 1; i < 10; i++) {
                        JSONObject locationJson = rootJson.getJSONObject(""+i);

                        String name = (String) locationJson.get("FAC_NAME");
                        Log.d(LOG_TAG, name);

                        String description = "Desc";

                        String phoneNumber = (String) locationJson.get("PHONE_NUMBER");
                        String address = (String) locationJson.get("ADDRESS");
                        String city = (String) locationJson.get("CITY");
                        String state = (String) locationJson.get("STATE");
                        String zip = (String) locationJson.get("ZIP");

                        double locationLat = locationJson.getDouble("LATITUDE");
                        double locationLong = locationJson.getDouble("LONGITUDE");

                        int facilityID = locationJson.getInt("FAC_ID");

                        Facility facility = new Facility(facilityID, name, phoneNumber, address, city, state, zip, locationLat, locationLong);

                        double userLocation[] = getGPSLocation();
                        double distance = 0;

                        if(userLocation[0] != 0 && userLocation[1] != 0) {
                            distance = distanceBetweenCoordinates(locationLat, locationLong, userLocation[0], userLocation[1], "M");

                            DecimalFormat df = new DecimalFormat("#.##");
                            description = "Distance: " + df.format(distance) + " miles";
                        }

                        addFacilityCard(name, description, phoneNumber, address, city, state);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
     * Load the facility image and place it into facilityImageView.
     * Try the street view image first, then the map image, then the default image.
     * @param facilityImageView The ImageView to place the image into
     * @param address The street address
     * @param city The city
     * @param state The state. Can be initials or full name.
     */
    private void loadFacilityImage(ImageView facilityImageView, String address, String city, String state) {
        loadStreetViewImage(facilityImageView, address, city, state);
    }

    /**
     * Load the street view imagery for the given address.
     * If there is no street view imagery, it uses the map view instead.
     * You should not call this directly. Call loadFacilityImage instead
     * @param imageView The ImageView to place the image into
     * @param address The street address
     * @param city The city
     * @param state The state. Can be initials or full name
     * @throws UnsupportedEncodingException
     */
    private void loadStreetViewImage(final ImageView imageView, final String address, final String city, final String state) {
        Log.d(LOG_TAG, "Entering load street view image.");

        String url = "";

        // If the street view url cannot be created, load the map view instead
        try {
            url = calculateStreetViewUrl(address, city, state);
        } catch (UnsupportedEncodingException e) {
            loadMapImage(imageView, address, city, state);
            return;
        }
        Log.d(LOG_TAG, url);

        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        Log.d(LOG_TAG, "Street View Image onResponse");

                        // If there is no street view image for the address use the map view instead
                        if(validStreetViewBitmap(bitmap))
                            imageView.setImageBitmap(bitmap);
                        else
                            loadMapImage(imageView, address, city, state);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "Street View Image errorListener: " + error.toString());

                        // Load the map view instead
                        loadMapImage(imageView, address, city, state);
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
     * @param address The street address
     * @param city The city
     * @param state The state. Can be initials or full name
     */
    private void loadMapImage(final ImageView imageView, String address, String city, String state) {
        Log.d(LOG_TAG, "Entering load map image.");

        final int defaultImageId = R.drawable.nspl;

        String url = null;
        try {
            url = calculateMapUrl(address, city, state);
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
                        Log.d(LOG_TAG, "IMAGE onResponse");
                        imageView.setImageBitmap(bitmap);
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
     * @param name The name of the facility
     * @param description A description of the facility
     * @param phone The phone number of the facility
     * @param address The street address of the facility
     * @param city The city of the facility
     * @param state The state. Can be initials or full name
     */
    private void addFacilityCard(final String name, String description, final String phone, String address, final String city, final String state) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        RelativeLayout cardRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.facility_layout, null, false);

        TextView nameTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_name_textview);
        nameTextView.setText(name);

        TextView descriptionTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_details);
        descriptionTextView.setText(description);

        TextView phoneTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_phone_textview);
        phoneTextView.setText(phone);
        phoneTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = getFirstPhoneNumber(phone);
                openDialer(phoneNumber);
            }
        });

        ImageView facilityImageView = (ImageView) cardRelativeLayout.findViewById(R.id.facility_imageview);
        loadFacilityImage(facilityImageView, address, city, state);
        facilityImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showMap(getMapUri(name, city, state));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        LinearLayout parentLinearLayout = (LinearLayout) getView().findViewById(R.id.facilities_linear_layout);
        parentLinearLayout.addView(cardRelativeLayout);
    }

    public void showMap(Uri geoLocation) {
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
