package com.tytanapps.ptsd;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.tytanapps.ptsd.fragments.MainFragment;
import com.tytanapps.ptsd.fragments.NearbyFacilitiesFragment;
import com.tytanapps.ptsd.fragments.PTSDTestFragment;
import com.tytanapps.ptsd.fragments.PhoneFragment;
import com.tytanapps.ptsd.fragments.ResourcesFragment;
import com.tytanapps.ptsd.fragments.WebsiteFragment;

import angtrim.com.fivestarslibrary.FiveStarsDialog;
import angtrim.com.fivestarslibrary.NegativeReviewListener;
import angtrim.com.fivestarslibrary.ReviewListener;

/**
 * The only activity in the app. Each screen of the app is a fragment. The user can switch
 * between them using the navigation view.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, RemoteConfigurable {

    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_CONTACT_REQUEST = 4;

    // The connection to the Google API
    private static GoogleApiClient mGoogleApiClient;

    // Whether the user is signed in to the app with their Google Account
    private boolean isUserSignedIn = false;

    // The header image on top of the navigation view containing the user's information
    private ViewGroup navHeader;

    // The request queue used to connect with APIs in the background
    private RequestQueue requestQueue;

    private static final int RC_SIGN_IN = 1;
    private static final int PICK_CONTACT_REQUEST = 2;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private boolean alreadySetPersistence = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState == null) {
                // Create a new Fragment to be placed in the activity layout
                MainFragment firstFragment = new MainFragment();

                // In case this activity was started with special instructions from an
                // Intent, pass the Intent's extras to the fragment as arguments
                firstFragment.setArguments(getIntent().getExtras());

                // Add the fragment to the 'fragment_container' FrameLayout
                getFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, firstFragment).commit();
            }
        }

        if(!alreadySetPersistence) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            alreadySetPersistence = true;
        }

        // Set up the side drawer layout containing the user's information and navigation items
        setupDrawerLayout();

        // Set up the trusted contact button
        setupFAB();

        // Set up the connection to the Google API Client. This does not sign in the user.
        setupGoogleSignIn();

        setupRemoteConfig();

        showRatingPrompt();
    }

    /**
     * Prompt the user to rate the app on Google Play
     * If the user selects 4/5 stars they are brought to the Play Store
     * If they select 1-3 it opens an email intent
     */
    private void showRatingPrompt() {
        int ratingPromptShowAfter = (int) mFirebaseRemoteConfig.getDouble("rating_prompt_show_after");
        int ratingUpperBound = (int) mFirebaseRemoteConfig.getDouble("rating_upper_bound");
        final String supportEmailAddress = mFirebaseRemoteConfig.getString("support_email_address");

        final String deviceInformation = getDeviceInformation();

        if(ratingPromptShowAfter > 0) {
            FiveStarsDialog fiveStarsDialog = new FiveStarsDialog(this, supportEmailAddress);
            fiveStarsDialog.setRateText("How well do you like PTSD Aid?")
                    .setTitle("Enjoying the app?")
                    .setForceMode(false)
                    .setUpperBound(ratingUpperBound) // Market opened if a rating >= 4 is selected
                    .setNegativeReviewListener(new NegativeReviewListener() {
                        @Override
                        public void onNegativeReview(int i) {
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                    "mailto", supportEmailAddress, null));
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PTSD Aid");
                            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello! I would like to give feedback on PTSD Aid!\n\nWhat I liked:\n\nWhat I didn't like:\n\n\nDevice Information:\n" + deviceInformation);
                            startActivity(Intent.createChooser(emailIntent, "Send email..."));
                        }
                    }) // OVERRIDE mail intent for negative review
                    .setReviewListener(new ReviewListener() {
                        @Override
                        public void onReview(int i) {

                        }
                    }) // Used to listen for reviews (if you want to track them )
                    .showAfter(ratingPromptShowAfter);
        }
    }

    private String getDeviceInformation() {
        String deviceInformation = "";

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;
            
            deviceInformation += "APP VERSION: " + version + " (" + verCode + ")\n";

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        
        deviceInformation += "SDK INT: " + Build.VERSION.RELEASE + " (" + Build.VERSION.SDK_INT + ")\n";
        //deviceInformation += "CODENAME: " + Build.VERSION.CODENAME + "\n";
        //deviceInformation += "INCREMENTAL: " + Build.VERSION.INCREMENTAL + "\n";
        //deviceInformation += "RELEASE: " + Build.VERSION.RELEASE + "\n";
        //deviceInformation += "BOARD: " + Build.BOARD + "\n";
        //deviceInformation += "BOOTLOADER: " + Build.BOOTLOADER + "\n";
        deviceInformation += "BRAND: " + Build.BRAND + "\n";
        deviceInformation += "DEVICE: " + Build.DEVICE + "\n";
        //deviceInformation += "DISPLAY: " + Build.DISPLAY + "\n";
        deviceInformation += "FP: " + Build.FINGERPRINT + "\n";
        //deviceInformation += "RADIO VERSION: " + Build.getRadioVersion() + "\n";
        //deviceInformation += "HARDWARE: " + Build.HARDWARE + "\n";
        //deviceInformation += "HOST: " + Build.HOST + "\n";
        //deviceInformation += "ID: " + Build.ID + "\n";
        deviceInformation += "MANUFACTURER: " + Build.MANUFACTURER + "\n";
        deviceInformation += "MODEL: " + Build.MODEL + "\n";
        deviceInformation += "PRODUCT: " + Build.PRODUCT + "\n";
        //deviceInformation += "SERIAL: " + Build.SERIAL + "\n";
        //deviceInformation += "TAGS: " + Build.TAGS + "\n";
        //deviceInformation += "TYPE: " + Build.TYPE + "\n";
        //deviceInformation += "UNKNOWN: " + Build.UNKNOWN + "\n";
        //deviceInformation += "USER: " + Build.USER + "\n";
        //deviceInformation += "TIME: " + Build.TIME + "\n";

        return deviceInformation;
    }

    private void setupRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        mFirebaseRemoteConfig.fetch(24 * 60 * 60) // cache for 24 hours
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(LOG_TAG, "Fetch Succeeded");
                        // Once the config is successfully fetched it must be activated before newly fetched
                        // values are returned.
                        mFirebaseRemoteConfig.activateFetched();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(LOG_TAG, "Fetch failed");
                    }
                });
    }

    public FirebaseRemoteConfig getRemoteConfig() {
        return mFirebaseRemoteConfig;
    }

    @Override
    public void onStart() {
        //Log.d(LOG_TAG, "onStart() called with: " + "");
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Attempt to sign the user in automatically
        silentSignIn();
    }

    @Override
    public void onStop() {
        //Log.d(LOG_TAG, "onStop() called with: " + "");
        super.onStop();
    }

    /**
     * When the back button is pressed, close the layout drawer or exit the app
     */
    @Override
    public void onBackPressed() {
        // Close the drawer layout if it is open
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        // Result returned when launching the pick trusted contact request
        else if(requestCode == PICK_CONTACT_REQUEST) {
            if(resultCode == RESULT_OK) {
                Uri contactUri = data.getData();
                handlePickContractRequest(contactUri);
            }
        }
    }

    /**
     * Set up the floating action button in the bottom of the app
     * When pressed, call the trusted contact if it exists. If not, show the create contact dialog
     */
    private void setupFAB() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if(fab != null) {
            // Call the trusted contact if it exists, otherwise show the create contact dialog
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String phoneNumber = getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "");
                    if (!phoneNumber.equals(""))
                        openDialer(phoneNumber);
                    else
                        showCreateTrustedContactDialog();
                }
            });

            // If you press and hold on the button, change the trusted contact
            fab.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showChangeTrustedContactDialog();
                    return true;
                }
            });
        }
    }

    /**
     * Create the client to connect with the Google sign in API
     */
    private void setupGoogleSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    /**
     * Set up the side drawer layout and populate it with the header and navigation items
     */
    private void setupDrawerLayout() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // The navigation view is contained within the drawer layout
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Add the header view containing the user's information
        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup navigationHeader = (ViewGroup) inflater.inflate(R.layout.nav_header_main, null, false);
        navigationHeader.findViewById(R.id.button_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        navigationView.addHeaderView(navigationHeader);
        navHeader = navigationHeader;
    }

    /**
     * Sign in to the user's Google Account
     */
    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * When the user has been logged in, update the UI
     * @param result The result of signing into your account
     */
    private void handleSignInResult(GoogleSignInResult result) {
        if (result != null && result.isSuccess()) {
            isUserSignedIn = true;

            GoogleSignInAccount googleAccount = result.getSignInAccount();

            String name = googleAccount.getDisplayName();
            String email = googleAccount.getEmail();
            Uri profilePicture = googleAccount.getPhotoUrl();

            View signInButton = findViewById(R.id.button_sign_in);
            if(signInButton != null)
                signInButton.setVisibility(View.INVISIBLE);

            updateNavigationHeader(name, email, profilePicture);
        }
    }

    /**
     * Sign in to the user's Google Account in the background
     * Only signs the user in if they have previously granted access
     */
    private void silentSignIn() {
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);

        if (pendingResult.isDone()) {
            // There's immediate result available.
            GoogleSignInResult googleAccount = pendingResult.get();
            handleSignInResult(googleAccount);
        } else {
            // There's no immediate result ready, displays some progress indicator and waits for the
            // async callback.
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    handleSignInResult(result);
                }
            });
        }
    }

    /**
     * Is the user signed in to the app with their Google Account
     * @return Whether the user is signed in to the app
     */
    public boolean isUserSignedIn() {
        return isUserSignedIn;
    }

    /**
     * Update the header on the navigation view with the user's information
     * @param name The name to display on the navigation view
     * @param email The email subtext to show on the navigation view
     * @param profilePicture A url of the user's profile picture
     */
    private void updateNavigationHeader(String name, String email, Uri profilePicture) {
        // Update the name
        TextView drawerNameTextView = (TextView) navHeader.findViewById(R.id.drawer_name);
        drawerNameTextView.setText(name);

        // Update the email address
        TextView drawerEmailTextView = (TextView) navHeader.findViewById(R.id.drawer_subtext);
        drawerEmailTextView.setVisibility(View.VISIBLE);
        drawerEmailTextView.setText(email);

        // Show the profile picture. If the user doesn't have one, use the app logo instead
        ImageView profileImageView = (ImageView) navHeader.findViewById(R.id.drawer_imageview);
        loadProfilePicture(profileImageView, profilePicture);

        // Hide the sign in button
        View signInButton = navHeader.findViewById(R.id.button_sign_in);
        if(signInButton != null)
            signInButton.setVisibility(View.GONE);
    }

    /**
     * Load the street view imagery for the given address.
     * If there is no street view imagery, it uses the map view instead.
     * You should not call this directly. Call loadFacilityImage instead
     * @param imageView The ImageView to place the image into
     * @param profilePictureUri The uri of the image to load
     */
    private void loadProfilePicture(final ImageView imageView, Uri profilePictureUri) {
        if(profilePictureUri == null || imageView == null)
            return;

        String url = profilePictureUri.toString();

        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        imageView.setImageBitmap(Utilities.getCircularBitmap(bitmap, 200, 200));
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "Street View Image errorListener: " + error.toString());
                    }
                });

        // Start loading the image in the background
        getRequestQueue().add(request);
    }

    /**
     * Display a dialog explaining a trusted contact and allow the user to make one
     */
    public void showCreateTrustedContactDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setPositiveButton(R.string.add_trusted_contact, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pickTrustedContact();
            }
        });
        alertDialogBuilder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.create_trusted_contact_layout, null, false);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setView(layout);
        alertDialog.show();
    }

    /**
     * Display a dialog explaining a trusted contact and allow the user to make one
     */
    protected void showChangeTrustedContactDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setPositiveButton(R.string.change_trusted_contact, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pickTrustedContact();
            }
        });

        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.change_trusted_contact_layout, null, false);

        TextView currentContactTextView = (TextView) layout.findViewById(R.id.current_contact_textview);

        String contactName = getSharedPreferenceString(getString(R.string.pref_trusted_name_key), "");
        String contactPhone = getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "");

        if(!contactPhone.equals("")) {
            currentContactTextView.setText("Your trusted contact is\n" + contactName + "\n" + contactPhone);

            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setView(layout);
            alertDialog.show();
        }
        else
            showCreateTrustedContactDialog();
    }

    private boolean contactsPermissionGranted() {
        // Assume thisActivity is the current activity
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestContactsPermission() {
        if (!contactsPermissionGranted()) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_CONTACT_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_CONTACT_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    pickTrustedContact();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("Please grant the contacts permission");
                    alertDialog.setMessage("Grant the contacts permission to continue");
                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            
                        }
                    });
                    alertDialog.create().show();

                    //errorLoadingResults(getString(R.string.error_location_permission));
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Read a shared preference string from memory
     * @param prefKey The key of the shared preference
     * @param defaultValue The value to return if the key does not exist
     * @return The shared preference with the given key
     */
    private String getSharedPreferenceString(String prefKey, String defaultValue) {
        return getPreferences(Context.MODE_PRIVATE).getString(prefKey, defaultValue);
    }

    /**
     * Open the dialer with a phone number entered
     * This does not call the number directly, the user needs to press the call button
     * @param phoneNumber The phone number to call
     */
    protected void openDialer(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(getBaseContext(), R.string.error_open_dialer, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open an intent to allow the user to pick one of their contacts
     */
    protected void pickTrustedContact() {
        if(!contactsPermissionGranted()) {
            requestContactsPermission();
        }
        else {

            try {
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
            } catch (ActivityNotFoundException activityNotFoundException) {
                Toast.makeText(getBaseContext(), R.string.error_choose_contact, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handle the result of choosing a trusted contact
     * @param trustedContactUri The response from the contact picker activity
     */
    private void handlePickContractRequest(Uri trustedContactUri) {
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor cursor = getContentResolver().query(trustedContactUri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String phoneNumber = cursor.getString(column);
            cursor.close();

            String name = getContactName(phoneNumber);

            saveSharedPreference(getString(R.string.pref_trusted_name_key), name);
            saveSharedPreference(getString(R.string.pref_trusted_phone_key), phoneNumber);

            Log.d(LOG_TAG, "Trusted contact changed: NAME: " + name + " PHONENUMBER: " + phoneNumber);
        }
    }

    /**
     * Get a contact's name given their phone number
     * @param phoneNumber The phone number of the contact
     * @return The contact's name
     */
    public String getContactName(String phoneNumber) {
        Log.d(LOG_TAG, "getContactName() called with: " + "phoneNumber = [" + phoneNumber + "]");

        ContentResolver cr = getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        Log.d(LOG_TAG, "getContactName() returned: " + contactName);
        return contactName;
    }

    /**
     * Save a String to a SharedPreference
     * @param prefKey The key of the shared preference
     * @param value The value to save in the shared preference
     */
    private void saveSharedPreference(String prefKey, String value) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(prefKey, value);
        editor.apply();
    }

    /**
     * Get the request queue. Creates it if it has not yet been instantiated
     * @return The request queue for connecting with an API
     */
    public RequestQueue getRequestQueue() {
        if(requestQueue == null)
            instantiateRequestQueue();

        return requestQueue;
    }

    /**
     * Create the request queue. This is used to connect to the API in the background
     */
    private void instantiateRequestQueue() {
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();
    }

    /**
     * Switch the fragment when a navigation item in the navigation pane is selected
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment newFragment = null;

        // Switch to the appropriate fragment
        int id = item.getItemId();
        switch(id) {
            case R.id.nav_simple_test:
                newFragment = new MainFragment();
                break;
            case R.id.nav_test:
                newFragment = new PTSDTestFragment();
                break;
            case R.id.nav_resources:
                newFragment = new ResourcesFragment();
                break;
            case R.id.nav_hotline:
                newFragment = new PhoneFragment();
                break;
            case R.id.nav_websites:
                newFragment = new WebsiteFragment();
                break;
            case R.id.nav_nearby:
                newFragment = new NearbyFacilitiesFragment();
                break;
        }

        if(newFragment != null) {
            switchFragment(newFragment);
        }

        // Close the drawer layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Switch to a new fragment
     * Precondition: newFragment is not null
     * @param newFragment The fragment to switch to
     */
    public void switchFragment(Fragment newFragment) {
        android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
    }

    /**
     * Purposely crash the app to test debugging
     */
    private void crashApp() {
        Log.e(LOG_TAG, "The crash app method has been called.");
        throw new RuntimeException("The crash app method has been called. What did you expect to happen?");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}
}
