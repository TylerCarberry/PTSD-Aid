package me.tylercarberry.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

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
        // Required empty public constructor
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

        loadNearbyFacilities();
    }


    public void loadNearbyFacilities() {

        Toast.makeText(getActivity(), "LOADING...", Toast.LENGTH_LONG).show();

        String url = "http://www.va.gov/webservices/fandl/facilities.cfc?method=Facility_byRegionIDandType_detail_array&fac_fld=NJ&fac_val=5,7&license="
                + getString(R.string.api_key_va_facilities)
                + "&ReturnFormat=JSON";

        RequestQueue requestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getActivity().getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(LOG_TAG, response);

                response = response.substring(2);

                try {
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");

                    for(int i = 1; i < 10; i++) {

                        JSONObject locationJson = rootJson.getJSONObject(""+i);

                        String name = (String) locationJson.get("FAC_NAME");
                        Log.d(LOG_TAG, name);


                        String description = "Desc";

                        String phoneNumber = (String) locationJson.get("PHONE_NUMBER");



                        double userLocation[] = getGPS();
                        double distance = 0;

                        if(userLocation[0] != 0 && userLocation[1] != 0) {
                            double locationLat = locationJson.getDouble("LATITUDE");
                            double locationLong = locationJson.getDouble("LONGITUDE");

                            distance = distanceBetweenCoordinates(locationLat, locationLong, userLocation[0], userLocation[1], "M");

                            DecimalFormat df = new DecimalFormat("#.##");
                            description = "Distance: " + df.format(distance) + " miles";
                        }

                        addFacilityCard(name, description, phoneNumber);
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

        //Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions.
        //Volley does retry for you if you have specified the policy.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }

    private void addFacilityCard(String name, String description, String phone) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        RelativeLayout cardRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.facility_layout, null, false);

        TextView nameTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_name_textview);
        nameTextView.setText(name);

        TextView descriptionTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_details);
        descriptionTextView.setText(description);

        TextView phoneTextView = (TextView) cardRelativeLayout.findViewById(R.id.facility_phone_textview);
        phoneTextView.setText(phone);


        LinearLayout parentLinearLayout = (LinearLayout) getView().findViewById(R.id.facilities_linear_layout);
        parentLinearLayout.addView(cardRelativeLayout);


    }


    private double[] getGPS() {
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
