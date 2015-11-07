package com.tytanapps.ptsd;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener{

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(LOG_TAG, "onCreate");

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String phoneNumber = getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "123-555-0000"); //getString(R.string.phone_suicide_lifeline));
                openDialer(phoneNumber);

                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup navigationHeader = (ViewGroup) inflater.inflate(R.layout.nav_header_main, null, false);


        TextView drawerNameTextView = (TextView) navigationHeader.findViewById(R.id.drawer_name);
        String name = getSharedPreferenceString(getString(R.string.pref_name_key), "DEFAULT NAME");
        if(name.equals(""))
            name = "DEFAULT NAME";
        drawerNameTextView.setText(name);

        navigationView.addHeaderView(navigationHeader);

    }

    private String getSharedPreferenceString(String prefKey, String defaultValue) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(prefKey, defaultValue);
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

    @Override
    public void onStart() {
        //Log.d(LOG_TAG, "onStart");

        AlarmService alarmService = new AlarmService(getBaseContext());
        alarmService.cancelAlarm();

        super.onStart();
    }

    @Override
    public void onStop() {
        //Log.d(LOG_TAG, "onStop");

        AlarmService alarmService = new AlarmService(getBaseContext());
        alarmService.startAlarm(24);

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment newFragment = null;

        switch(id) {
            case R.id.nav_simple_test:
                newFragment = new MainFragment();
                break;
            case R.id.nav_test:
                newFragment = new StressTestFragment();
                break;
            case R.id.nav_resources:
                newFragment = new ResourcesFragment();
                break;
            case R.id.nav_manage:
                newFragment = new ManageFragment();
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
            android.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
