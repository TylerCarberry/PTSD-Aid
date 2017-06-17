package com.tytanapps.ptsd;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tytanapps.ptsd.dialog.FeedbackDialog;
import com.tytanapps.ptsd.dialog.RatingDialog;
import com.tytanapps.ptsd.va.facility.FacilitiesFragment;
import com.tytanapps.ptsd.network.PtsdMessagingService;
import com.tytanapps.ptsd.network.RemoteConfig;
import com.tytanapps.ptsd.fragments.MainFragment;
import com.tytanapps.ptsd.fragments.PTSDTestFragment;
import com.tytanapps.ptsd.fragments.ResourcesFragment;
import com.tytanapps.ptsd.settings.SettingsFragment;
import com.tytanapps.ptsd.va.news.NewsFragment;
import com.tytanapps.ptsd.support.phone.PhoneFragment;
import com.tytanapps.ptsd.settings.Preferences;
import com.tytanapps.ptsd.utils.ExternalAppUtil;
import com.tytanapps.ptsd.utils.PermissionUtil;
import com.tytanapps.ptsd.utils.PtsdUtil;
import com.tytanapps.ptsd.utils.StringUtil;
import com.tytanapps.ptsd.support.website.WebsiteFragment;

import javax.inject.Inject;

import angtrim.com.fivestarslibrary.NegativeReviewListener;
import angtrim.com.fivestarslibrary.ReviewListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static butterknife.ButterKnife.findById;
import static com.tytanapps.ptsd.RequestCodes.REQUEST_CONTACT_PERMISSION;
import static com.tytanapps.ptsd.RequestCodes.REQUEST_PICK_TRUSTED_CONTACT;
import static com.tytanapps.ptsd.RequestCodes.REQUEST_SIGN_IN;

/**
 * The only activity in the app. Each screen of the app is a fragment. The user can switch
 * between them using the navigation view.
 */
public class MainActivity extends AppCompatActivity
        implements  NavigationView.OnNavigationItemSelectedListener,
                    GoogleApiClient.OnConnectionFailedListener {

    // Whether the user is signed in to the app with their Google Account
    private boolean isUserSignedIn = false;

    // Header image on top of the navigation view containing the user's information
    private ViewGroup navHeader;

    @Inject RemoteConfig remoteConfig;
    @Inject FirebaseMessaging firebaseMessaging;
    @Inject GoogleApiClient googleApiClient;
    @Inject Preferences preferences;

    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.fab) FloatingActionButton fab;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((PTSDApplication)getApplication()).getPtsdComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("notification_action");
            if (value != null && value.equals("unsubscribe")) {
                unsubscribeNewsNotifications();
                Snackbar.make(findViewById(R.id.fragment_container), R.string.unsubscribed_news_message, Snackbar.LENGTH_LONG).show();

                // Dismiss the notification
                NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
                manager.cancel(PtsdMessagingService.NOTIFICATION_ID);
            }
        }

        // Set up the side drawer layout containing the user's information and navigation items
        setupDrawerLayout();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState == null) {
                Fragment firstFragment;

                if (extras != null && extras.getString("fragment") != null) {
                    firstFragment = getFirstFragment(extras.getString("fragment"));
                }
                else {
                    firstFragment = new MainFragment();

                    if (!BuildConfig.DEBUG)
                        showRatingPrompt();
                }

                // In case this activity was started with special instructions from an
                // Intent, pass the Intent's extras to the fragment as arguments
                firstFragment.setArguments(getIntent().getExtras());

                // Add the fragment to the 'fragment_container' FrameLayout
                getFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, firstFragment).commit();

                currentFragment = firstFragment;
            }
        }

        firebaseMessaging.subscribeToTopic(BuildConfig.DEBUG ? "debug" : "release");
        setupNewsNotifications();
    }

    @Override
    public void onStart() {
        super.onStart();
        fab.setVisibility(shouldShowFab() ? View.VISIBLE : View.INVISIBLE);
        googleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        silentGoogleSignIn();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    /**
     * When the back button is pressed, close the layout drawer or exit the app
     */
    @Override
    public void onBackPressed() {
        // Close the drawer layout if it is open
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            case REQUEST_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
                break;
            // Result returned when launching the pick trusted contact request
            case REQUEST_PICK_TRUSTED_CONTACT:
                if (resultCode == RESULT_OK) {
                    Uri contactUri = data.getData();
                    handlePickContactRequest(contactUri);
                }
        }
    }

    private boolean shouldShowFab() {
        return preferences.getBoolean("enable_trusted_contact", true);
    }

    /**
     * Unsubscribe the user from notifications regarding new va news
     */
    private void unsubscribeNewsNotifications() {
        preferences.set(R.string.pref_news_notification, false);
        firebaseMessaging.unsubscribeFromTopic("news");
    }

    /**
     * Get the first fragment that should be shown to the user when the app is opened
     * @param extrasFragment The string extra that was passed in the intent
     * @return The fragment to display on screen
     */
    @NonNull
    private Fragment getFirstFragment(String extrasFragment) {
        Fragment firstFragment;
        switch (extrasFragment) {
            case "main":
                firstFragment = new MainFragment();
                break;
            case "test":
                firstFragment = new PTSDTestFragment();
                break;
            case "resources":
                firstFragment = new ResourcesFragment();
                break;
            case "facilities":
                firstFragment = new FacilitiesFragment();
                break;
            case "news":
                firstFragment = new NewsFragment();
                break;
            case "phone":
                firstFragment = new PhoneFragment();
                break;
            case "website":
                firstFragment = new WebsiteFragment();
                break;
            default:
                firstFragment = new MainFragment();
                break;
        }
        return firstFragment;
    }

    /**
     * Subscribe or unsubscribe the user from news notifications depending on the shared preference
     */
    private void setupNewsNotifications() {
        if (preferences.getBoolean(R.string.pref_news_notification, true)) {
            firebaseMessaging.subscribeToTopic("news");
        } else {
            firebaseMessaging.unsubscribeFromTopic("news");
        }
    }

    /**
     * Prompt the user to rate the app on Google Play
     * If the user selects 4/5 stars they are brought to the Play Store
     * If they select 1-3 it opens an email intent
     */
    private void showRatingPrompt() {
        int ratingPromptShowAfter = remoteConfig.getInt(R.string.rc_rating_prompt_show_after);
        final String supportEmailAddress = remoteConfig.getString(R.string.rc_support_email_address);

        if (ratingPromptShowAfter > 0) {
            RatingDialog ratingDialog = new RatingDialog(this, supportEmailAddress);
            ratingDialog.setNegativeReviewListener(new NegativeReviewListener() {
                        @Override
                        public void onNegativeReview(int i) {
                            provideFeedback();
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

    /**
     * Open a new Doorbell dialog asking the user for feedback
     */
    public void provideFeedback() {
        new FeedbackDialog(this).show();
    }

    /**
     * Set up the side drawer layout and populate it with the header and navigation items
     */
    private void setupDrawerLayout() {
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0); // disable the animation
            }
        };

        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        // Add the header view containing the user's information
        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup navigationHeader = (ViewGroup) inflater.inflate(R.layout.nav_header_main, rootViewGroup(), false);

        // The navigation view is contained within the drawer layout
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.addHeaderView(navigationHeader);
        }
        navHeader = navigationHeader;
    }

    /**
     * Sign in to the user's Google Account
     */
    public void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, REQUEST_SIGN_IN);
    }

    /**
     * When the user has been logged in, update the UI
     * @param result The result of signing into your account
     */
    private void handleSignInResult(GoogleSignInResult result) {
        if (result != null && result.isSuccess()) {
            isUserSignedIn = true;

            GoogleSignInAccount googleAccount = result.getSignInAccount();
            if (googleAccount != null) {
                String name = googleAccount.getDisplayName();
                String email = googleAccount.getEmail();

                updateNavigationHeader(name, email);
            }
        }
    }

    /**
     * Sign in to the user's Google Account in the background
     * Only signs the user in if they have previously granted access
     */
    private void silentGoogleSignIn() {
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(googleApiClient);

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
     */
    private void updateNavigationHeader(String name, String email) {
        // Update the name
        TextView drawerNameTextView = findById(navHeader, R.id.drawer_name);
        drawerNameTextView.setText(name);
    }

    /**
     * Call the trusted contact. Show the create trusted contact dialog if not created
     */
    @OnClick(R.id.fab)
    public void callTrustedContact() {
        String phoneNumber = preferences.getString(R.string.pref_trusted_phone_key);
        if (!StringUtil.isNullOrEmpty(phoneNumber)) {
            ExternalAppUtil.openDialer(this, phoneNumber);
        } else {
            showCreateTrustedContactDialog();
        }
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

        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.create_trusted_contact_layout, rootViewGroup(), false);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setView(layout);
        alertDialog.show();
    }

    /**
     * Display a dialog explaining a trusted contact and allow the user to make one
     */
    @OnLongClick(R.id.fab)
    protected boolean showChangeTrustedContactDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setPositiveButton(R.string.change_trusted_contact, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pickTrustedContact();
            }
        });

        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.change_trusted_contact_layout, rootViewGroup(), false);

        TextView currentContactTextView = (TextView) layout.findViewById(R.id.current_contact_textview);

        String contactName = preferences.getString(R.string.pref_trusted_name_key);
        String contactPhone = preferences.getString(R.string.pref_trusted_phone_key);

        if (!contactPhone.isEmpty()) {
            currentContactTextView.setText("Your trusted contact is\n" + contactName + "\n" + contactPhone);

            final AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setView(layout);
            alertDialog.show();
        }
        else {
            showCreateTrustedContactDialog();
        }

        return true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CONTACT_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted
                    pickTrustedContact();
                }
                else {
                    // Permission denied
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.contacts_permission_title)
                        .setMessage(R.string.contacts_permission_message)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            // When the ok button is pressed, dismiss the dialog and do not do anything
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                    alertDialog.create().show();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Open an intent to allow the user to pick one of their contacts
     */
    public void pickTrustedContact() {
        if (!PermissionUtil.contactsPermissionGranted(this)) {
            PermissionUtil.requestContactsPermission(this);
        }
        else {
            try {
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(pickContactIntent, REQUEST_PICK_TRUSTED_CONTACT);
            } catch (ActivityNotFoundException activityNotFoundException) {
                Snackbar.make(findViewById(R.id.fragment_container), R.string.error_choose_contact, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Handle the result of choosing a trusted contact
     * @param trustedContactUri The response from the contact picker activity
     */
    private void handlePickContactRequest(Uri trustedContactUri) {
        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor cursor = getContentResolver().query(trustedContactUri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String phoneNumber = cursor.getString(column);
            cursor.close();

            String name = PtsdUtil.getContactName(this, phoneNumber);
            preferences.set(R.string.pref_trusted_name_key, name);
            preferences.set(R.string.pref_trusted_phone_key, phoneNumber);
        }
    }

    /**
     * Switch the fragment when a navigation item in the navigation pane is selected
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment newFragment = null;

        // Switch to the appropriate fragment
        int id = item.getItemId();
        switch(id) {
            case R.id.nav_recommendations:
                newFragment = MainFragment.newInstance();
                break;
            case R.id.nav_test:
                newFragment = PTSDTestFragment.newInstance();
                break;
            case R.id.nav_resources:
                newFragment = ResourcesFragment.newInstance();
                break;
            case R.id.nav_hotline:
                newFragment = PhoneFragment.newInstance();
                break;
            case R.id.nav_websites:
                newFragment = WebsiteFragment.newInstance();
                break;
            case R.id.nav_settings:
                newFragment = SettingsFragment.newInstance();
                break;
            case R.id.nav_facilities:
                newFragment = FacilitiesFragment.newInstance();
                break;
            case R.id.nav_news:
                newFragment = NewsFragment.newInstance();

                //Bundle bundle = new Bundle();
                //bundle.putString("param1", "From Activity");
                //newFragment.setArguments(bundle);
                break;
        }

        if (newFragment != null) {
            switchFragment(newFragment);
        }

        closeDrawerLayout();

        return true;
    }

    private void closeDrawerLayout() {
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * Switch to a new fragment
     * Precondition: newFragment is not null
     * @param newFragment The fragment to switch to
     */
    public void switchFragment(@NonNull Fragment newFragment) {
        if (currentFragment == null || !(newFragment.getClass().equals(currentFragment.getClass()))) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
            currentFragment = newFragment;
        }
    }

    private ViewGroup rootViewGroup() {
        View root = findViewById(R.id.drawer_layout);
        if (root != null && root instanceof ViewGroup)
            return (ViewGroup) root;
        return null;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}
}
