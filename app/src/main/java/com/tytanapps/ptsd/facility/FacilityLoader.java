package com.tytanapps.ptsd.facility;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.tytanapps.ptsd.LocationNotFoundException;
import com.tytanapps.ptsd.maps.MapsClient;
import com.tytanapps.ptsd.maps.MapsResult;
import com.tytanapps.ptsd.PTSDApplication;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.firebase.RemoteConfig;

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

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.tytanapps.ptsd.utils.PtsdUtil.distanceBetweenCoordinates;
import static com.tytanapps.ptsd.utils.PtsdUtil.getFirstPhoneNumber;
import static com.tytanapps.ptsd.utils.PtsdUtil.getGPSLocation;
import static com.tytanapps.ptsd.utils.PtsdUtil.loadBitmapFromFile;
import static com.tytanapps.ptsd.utils.PtsdUtil.readBitmapFromUrl;
import static com.tytanapps.ptsd.utils.PtsdUtil.readFromUrl;
import static com.tytanapps.ptsd.utils.PtsdUtil.saveBitmapToFile;
import static rx.Observable.just;

/**
 * Load the VA facilities that offer PTSD programs
 */
public abstract class FacilityLoader {
    private Fragment fragment;

    // Stores the facilities that have already loaded
    // Key: VA Id, Value: The facility with the given id
    private HashMap<Integer, Facility> knownFacilities = new HashMap<>();

    private final Trace facilitiesTrace;

    @Inject RemoteConfig remoteConfig;
    @Inject OkHttpClient okHttpClient;
    @Inject FirebasePerformance performance;
    @Inject MapsClient mapsClient;

    public FacilityLoader(Fragment fragment) {
        this.fragment = fragment;
        ((PTSDApplication)fragment.getActivity().getApplication()).getPtsdComponent().inject(this);
        facilitiesTrace = performance.newTrace("facilities_trace");
    }

    public abstract void errorLoadingResults(Throwable throwable);
    public abstract void onSuccess(List<Facility> loadedFacilities);
    public abstract void onLoadedImage(int facilityId);

    /**
     * Load all PTSD programs and the facility id where they are located.
     * There are multiple PTSD programs per VA facility.
     */
    public void loadPTSDPrograms() {
        facilitiesTrace.start();

        Observable<Facility> facilityObservable = Observable.just(buildPTSDUrl())
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        try {
                            return readFromUrl(okHttpClient, s);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                }).flatMap(new Func1<String, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(String response) {
                        // The JSON that the sever responds starts with //
                        // Trim the first two characters to create valid JSON.
                        response = response.substring(2);

                        // Load the initial JSON request. This this is a program name and the
                        // facility ID where it is located.
                        try {
                            JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");
                            int numberOfResults = new JSONObject(response).getInt("MATCHES");

                            if (numberOfResults == 0) {
                                throw new RuntimeException(fragment.getString(R.string.va_loading_error));
                            }

                            double[] userLocation = getGPSLocation(fragment.getActivity());
                            // If the user's GPS location cannot be found
                            if (userLocation[0] == 0 && userLocation[1] == 0) {
                                throw new LocationNotFoundException();
                            }

                            // Add each PTSD program to the correct VA facility
                            for (int i = 1; i < numberOfResults; i++) {
                                JSONObject ptsdProgramJson = rootJson.getJSONObject("" + i);
                                addPTSDProgram(ptsdProgramJson);
                            }
                            return Observable.from(knownFacilities.keySet());
                        } catch (JSONException e) {
                            FirebaseCrash.report(e);
                            e.printStackTrace();
                            return null;
                        }
                        // Get the facility given its id
                    }
                }).flatMap(new Func1<Integer, Observable<Facility>>() {
                    @Override
                    public Observable<Facility> call(Integer facilityId) {
                        return loadFacility(facilityId);
                    }
                });

        facilityObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Facility>() {
                    @Override
                    public void onCompleted() {
                        allFacilitiesHaveLoaded();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Error loading ptsd programs");
                        facilitiesTrace.stop();
                        errorLoadingResults(e);
                    }

                    @Override
                    public void onNext(Facility facility) {
                        knownFacilities.put(facility.getFacilityId(), facility);
                    }
                });
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
        if (knownFacilities.containsKey(facilityID)) {
            facility = knownFacilities.get(facilityID);
        } else {
            facility = new Facility(facilityID);
        }

        facility.addProgram(programName);
        knownFacilities.put(facilityID, facility);
    }


    /**
     * Load a VA facility from the VA api, using cache if available
     * @param facilityId The id of the facility to load
     * @return An observable for the facility
     */
    private Observable<Facility> loadFacility(int facilityId) {
        return Observable.concat(
                    Observable.just(readCachedFacility(facilityId)),
                    loadFacilityFromNetwork(facilityId))
                .filter(new Func1<Facility, Boolean>() {
                    @Override
                    public Boolean call(Facility facility) {
                        return facility != null;
                    }
                }).first();
    }


    /**
     * Load a VA facility from the VA api
     * @param facilityId The id of the facility to fetch
     * @return An observable for the facility
     */
    private Observable<Facility> loadFacilityFromNetwork(int facilityId) {

        return just(facilityId).map(new Func1<Integer, Facility>() {
            @Override
            public Facility call(Integer facilityId1) {
                try {
                    String response = readFromUrl(okHttpClient, buildFacilityUrl(facilityId1, fragment.getString(R.string.api_key_va_facilities)));
                    // The JSON that the sever responds starts with //
                    // I am cropping the first two characters to create valid JSON.
                    response = response.substring(2);

                    // Get all of the information about the facility
                    JSONObject rootJson = new JSONObject(response).getJSONObject("RESULTS");
                    Facility facility =  parseJSONFacility(facilityId1, rootJson);
                    cacheFacility(facility);
                    return facility;
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });
    }


    /**
     * Convert a JSON VA facility into a Facility object
     * @param facilityId The unique id of the facility
     * @param rootJson The json representing the facility
     * @return The converted Facility
     * @throws JSONException If the JSON is improperly formed
     */
    private Facility parseJSONFacility(int facilityId, JSONObject rootJson) throws JSONException {
        Facility facility = new Facility(facilityId);

        JSONObject locationJson = rootJson.getJSONObject("1");

        String name = locationJson.getString("FAC_NAME");
        String phoneNumber = locationJson.getString("PHONE_NUMBER");
        String address = locationJson.getString("ADDRESS");
        String city = locationJson.getString("CITY");
        String state = locationJson.getString("STATE");
        String zip = locationJson.getString("ZIP");
        double locationLat = locationJson.getDouble("LATITUDE");
        double locationLong = locationJson.getDouble("LONGITUDE");
        String description = "";
        String url = getFacilityUrl(locationJson);


        double userLocation[] = getGPSLocation(fragment.getActivity());
        // The description contains the distance and all PTSD programs located there
        if(userLocation[0] != 0 && userLocation[1] != 0) {
            double distance = distanceBetweenCoordinates(locationLat, locationLong, userLocation[0], userLocation[1]);
            facility.setDistance(distance);

            DecimalFormat df = new DecimalFormat("#.##");
            description = "Distance: " + df.format(distance) + " miles";
        }

        if (remoteConfig.getBoolean(R.string.rc_show_va_programs)) {
            description += "\n";
            Set<String> programs = facility.getPrograms();
            for(String program : programs)
                description += "\n" + program;
        }

        facility.setName(name);
        facility.setPhoneNumber(getFirstPhoneNumber(phoneNumber));
        facility.setUrl(url);
        facility.setAddress(address, city, state, zip);
        facility.setDescription(description);
        facility.setLatitude(locationLat);
        facility.setLongitude(locationLong);

        return facility;
    }

    /**
     * Get the website of the VA facility
     * @param locationJson The JSON object representing the facility
     * @return The url of the facility
     * @throws JSONException The facility json is not valid
     */
    private String getFacilityUrl(JSONObject locationJson) throws JSONException {
        // The facility urls start with vaww. instead of www.
        // These cannot be loaded on the public internet so use www. instead.
        String url = (String) locationJson.get("FANDL_URL");
        url = url.replace("vaww", "www");
        return url;
    }


    /**
     * Load the Google Maps imagery for the given facility
     * @param facility The facility to load the imagery for
     */
    public void loadFacilityImage(final Facility facility) {
        int imageWidth = remoteConfig.getInt(R.string.rc_map_width);
        int imageHeight = remoteConfig.getInt(R.string.rc_map_height);

        Observable<Bitmap> bitmapObservable = Observable.concat(
                loadCacheFacilityImage(facility.getFacilityId()),
                loadStreetViewImage(facility, imageWidth, imageHeight),
                loadMapImage(facility, imageWidth, imageHeight))
                .filter(new Func1<Bitmap, Boolean>() {
                    @Override
                    public Boolean call(Bitmap bitmap) {
                        return bitmap != null;
                    }
                }).first();

        Observer<Bitmap> bitmapObserver = new Observer<Bitmap>() {
            @Override
            public void onCompleted() {
                onLoadedImage(facility.getFacilityId());
            }

            @Override
            public void onError(Throwable e) {}

            @Override
            public void onNext(Bitmap bitmap) {
                facility.setFacilityImage(bitmap);
            }
        };

        bitmapObservable
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmapObserver);
    }

    /**
     * Load the street view imagery for the given address.
     * If there is no street view imagery, it uses the map view instead.
     * You should not call this directly. Call loadFacilityImage instead
     * @param facility The facility
     */
    private Observable<Bitmap> loadStreetViewImage(final Facility facility, final int imageWidth, final int imageHeight) {
        try {
            return Observable
                    .just(buildStreetViewUrl(facility.getStreetAddress(), facility.getCity(), facility.getState(), imageWidth, imageHeight))
                    .filter(new Func1<String, Boolean>() {
                        @Override
                        public Boolean call(String s) {
                            return isStreetViewAvailableAtAddress(facility.getStreetAddress(), facility.getCity(), facility.getState());
                        }
                    })
                    .map(new Func1<String, Bitmap>() {
                @Override
                public Bitmap call(String url) {
                    try {
                        Bitmap bitmap = readBitmapFromUrl(url);
                        saveFacilityImage(bitmap, facility.getFacilityId());
                        return bitmap;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Observable.just(null);
    }

    /**
     * Load the Google Maps imagery for the given address.
     * If there is no map imagery, it uses the default image instead.
     * You should not call this directly. Call loadFacilityImage instead
     * @param facility The facility
     */
    private Observable<Bitmap> loadMapImage(final Facility facility, int imageWidth, int imageHeight) {
        try {
            return Observable.just(buildMapUrl(facility.getStreetAddress(), facility.getCity(), facility.getState(), imageWidth, imageHeight)).map(new Func1<String, Bitmap>() {
                @Override
                public Bitmap call(String url) {
                    try {
                        Bitmap bitmap = readBitmapFromUrl(url);
                        saveFacilityImage(bitmap, facility.getFacilityId());
                        return bitmap;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Observable.just(null);
    }

    /**
     * Called when all facilities have loaded and knownValues is fully populated
     */
    private void allFacilitiesHaveLoaded() {
        facilitiesTrace.stop();

        ArrayList<Facility> facilitiesList = new ArrayList<>();
        facilitiesList.addAll(knownFacilities.values());

        // Sort the facilities by distance
        Collections.sort(facilitiesList);

        onSuccess(facilitiesList);
    }

    /**
     * Clear the cache and reload the facilities from the network
     */
    public void refresh() {
        clearFacilityCache();
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
        saveBitmapToFile(file, bitmap);
    }

    /**
     * Load the facility image from a file.
     * @param facilityId The id of the facility
     * @return The facility image. Null if the file does not exist
     */
    public Observable<Bitmap> loadCacheFacilityImage(int facilityId) {
        File file = getFacilityImageFile(facilityId);
        return Observable.just(loadBitmapFromFile(file));
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
     * Clear the cached news articles
     */
    private void clearFacilityCache() {
        for(int facilityId : knownFacilities.keySet()) {
            File file = getFacilityFile(facilityId);
            if(file.exists()) {
                file.delete();
            }

            file = getFacilityImageFile(facilityId);
            if(file.exists()) {
                file.delete();
            }
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

            double userLocation[] = getGPSLocation(fragment.getActivity());
            double distance;

            String description = "";

            // The description contains the distance and all PTSD programs located there
            if(userLocation[0] != 0 && userLocation[1] != 0) {
                distance = distanceBetweenCoordinates(facility.getLatitude(), facility.getLongitude(), userLocation[0], userLocation[1]);
                facility.setDistance(distance);

                DecimalFormat df = new DecimalFormat("#.##");
                description = "Distance: " + df.format(distance) + " miles";
            }

            if (remoteConfig.getBoolean(R.string.rc_show_va_programs)) {
                description += "\n";
                Set<String> programs = facility.getPrograms();
                for (String program : programs) {
                    description += "\n" + program;
                }
            }

            facility.setDescription(description);

            input.close();
        } catch (FileNotFoundException ignored) {
            // If the file was not found, nothing is wrong.
            // It just means that the facility has not yet been cached.
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (facility != null) {
            facilitiesTrace.incrementCounter("facility_cache_hit");
        } else {
            facilitiesTrace.incrementCounter("facility_cache_miss");
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
    private String buildStreetViewUrl(String address, String town, String state, int imageWidth, int imageHeight) throws UnsupportedEncodingException {
        String location = encodeAddress(address, town, state);

        Uri builtUri = Uri.parse("https://maps.googleapis.com/maps/api/streetview")
                .buildUpon()
                .appendQueryParameter("size", imageWidth+"x"+imageHeight)
                .appendQueryParameter("location", location)
                .build();

        return builtUri.toString();
    }

    private boolean isStreetViewAvailableAtAddress(String address, String town, final String state) {
        try {
            Response<MapsResult> mapsResult = mapsClient.getMapMetadata((encodeAddress(address, town, state)), fragment.getString(R.string.api_key_google))
                    .execute();
            if (mapsResult != null && mapsResult.body() != null) {
                return mapsResult.body().isStreetViewAvailable();
            }
            return false;
        } catch (IOException e) {
            FirebaseCrash.report(e);
            return false;
        }
    }


    /**
     * Get the url for the Google Maps Api
     * @param address The street address
     * @param town The town
     * @param state The state. Can be initials or full name
     * @return The url for the street view api
     * @throws UnsupportedEncodingException If the address cannot be encoded into a url
     */
    private String buildMapUrl(String address, String town, String state, int imageWidth, int imageHeight) throws UnsupportedEncodingException {
        String location = encodeAddress(address, town, state);

        Uri mapUri = Uri.parse("http://maps.google.com/maps/api/staticmap")
                .buildUpon()
                .appendQueryParameter("center", location)
                .appendQueryParameter("zoom", "16")
                .appendQueryParameter("size", imageWidth+"x"+imageHeight)
                .appendQueryParameter("sensor", "false")
                .appendQueryParameter("markers", "color:redzlabel:A%7C\" + paramLocation")
                .build();

        return mapUri.toString();
    }

    /**
     * Get the url for the PTSD Programs API
     * @return The url for the PTSD Programs API
     */
    private String buildPTSDUrl() {
        Uri builtUri = Uri.parse("https://www.va.gov/webservices/PTSD/ptsd.cfc")
                .buildUpon()
                .appendQueryParameter("method", "PTSD_Program_Locator_array")
                .appendQueryParameter("license", fragment.getString(R.string.api_key_ptsd_programs))
                .appendQueryParameter("ReturnFormat", "JSON")
                .build();

        return builtUri.toString();
    }

    /**
     * Get the url for the VA facility API
     * @param facilityId The id of the facility to load
     * @param licenceKey The API licence key
     * @return The url for the VA facility API
     */
    private String buildFacilityUrl(int facilityId, String licenceKey) {
        Uri builtUri = Uri.parse("https://www.va.gov/webservices/fandl/facilities.cfc")
                .buildUpon()
                .appendQueryParameter("method", "GetFacsDetailByFacID_array")
                .appendQueryParameter("fac_id", ""+facilityId)
                .appendQueryParameter("license", licenceKey)
                .appendQueryParameter("ReturnFormat", "JSON")
                .build();

        return builtUri.toString();
    }

    /**
     * Encode an address to be used in Google Maps
     * @param address The street address
     * @param town The city/town
     * @param state Can be represented with either the full name or initials (New Jersey or NJ)
     * @return The encoded address to be used with the Google Maps API
     * @throws UnsupportedEncodingException The address was unable to be encoded
     */
    private String encodeAddress(String address, String town, String state) throws UnsupportedEncodingException {
        // Encode the address
        String location = address + ", " + town + ", " + state;
        location = URLEncoder.encode(location, "UTF-8");
        return location;
    }

}
