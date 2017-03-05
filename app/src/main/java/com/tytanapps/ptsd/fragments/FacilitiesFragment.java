package com.tytanapps.ptsd.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.tytanapps.ptsd.Facility;
import com.tytanapps.ptsd.FacilityAdapter;
import com.tytanapps.ptsd.FacilityLoader;
import com.tytanapps.ptsd.LocationNotFoundException;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.Utilities;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Loads a list of nearby VA facilities that offer PTSD programs.
 * Displays the address, phone number, programs, and an image for each facility.
 * You can call them, get directions, or load the website for each VA facility.
 */
public class FacilitiesFragment extends AnalyticsFragment {

    private static final String LOG_TAG = FacilitiesFragment.class.getSimpleName();

    private static final int PERMISSION_LOCATION_REQUEST = 3;

    private FacilityLoader facilityLoader;

    private final List<Facility> facilityList = new ArrayList<>();
    private FacilityAdapter mAdapter;

    private Unbinder unbinder;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.facility_loading_textview) TextView loadingTextView;
    @BindView(R.id.facility_progressbar) View loadingProgressBar;
    @BindView(R.id.retry_load_button) Button retryLoadButton;

    public FacilitiesFragment() {
        // Required default constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        facilityLoader = new FacilityLoader(this) {
            @Override
            public void errorLoadingResults(Throwable throwable) {
                FacilitiesFragment.this.errorLoadingResults(throwable);
            }

            @Override
            public void onSuccess(List<Facility> loadedFacilities) {
                FacilitiesFragment.this.allFacilitiesHaveLoaded(loadedFacilities);
            }

            @Override
            public void onLoadedImage(int facilityId) {}
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_nearby_facilities, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        swipeRefreshLayout.setEnabled(false);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * When the fragment becomes visible, start loading the VA facilities
     */
    @Override
    public void onStart() {
        super.onStart();

        // Load the VA facilities if they have not yet been loaded
        if(facilityList.size() == 0) {
            loadFacilities();
        }

        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_nearby).setChecked(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.facilities_menu, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(mAdapter != null)
                    mAdapter.filter(query);
                scrollFacilityListToTop();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mAdapter != null)
                    mAdapter.filter(newText);
                scrollFacilityListToTop();
                return true;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissKeyboard();
    }

    /**
     * Scroll the facility recycler view to the top
     */
    private void scrollFacilityListToTop() {
        recyclerView.scrollToPosition(0);
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
        mAdapter = new FacilityAdapter(facilityList, this, Utilities.getRemoteConfigInt(this, R.string.rc_facilities_to_display));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFacilities();
            }
        });

    }

    /**
     * Refresh the facilities
     */
    public void refreshFacilities() {
        mAdapter.notifyDataSetChanged();
        facilityLoader.refresh();
    }

    /**
     * When all of the facilities have fully loaded, sort them by distance and display them to the user
     */
    private void allFacilitiesHaveLoaded(final List<Facility> facilities) {
        hideLoadingBar();

        facilityList.clear();
        for(Facility facility : facilities) {
            facilityList.add(facility);
        }

        setupRecyclerView();
        enableRefreshLayout();

        if(mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    /**
     * Enable the refresh layout and stop the refreshing animation
     */
    private void enableRefreshLayout() {
        if(swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted
                    facilityLoader.loadPTSDPrograms();

                } else {
                    // Permission denied
                    errorLoadingResults(getString(R.string.error_location_permission));
                }
                break;
            }

            // Other 'case' lines to check for other permissions this app might request
        }
    }

    /**
     * There was an error loading the VA facilities. Display an error message
     */
    private void errorLoadingResults(Throwable throwable) {
        if(throwable instanceof LocationNotFoundException)
            errorLoadingResults(getString(R.string.gps_error));
        else
            errorLoadingResults(getString(R.string.va_loading_error));
    }

    /**
     * There was an error loading the VA facilities. Display a message and a retry button.
     * @param errorMessage The message to show to the user
     */
    private void errorLoadingResults(String errorMessage) {
        if(getView() != null) {
            swipeRefreshLayout.setRefreshing(false);
            if (facilityList.size() > 0) {
                Snackbar.make(getView(), R.string.error_va_facilities, Snackbar.LENGTH_SHORT).show();
            } else {
                loadingTextView.setVisibility(View.VISIBLE);
                loadingTextView.setText(errorMessage);
                retryLoadButton.setVisibility(View.VISIBLE);
                loadingProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Retry loading the facilities after an error has occurred
     */
    @OnClick(R.id.retry_load_button)
    public void retryLoadFacilities() {
        retryLoadButton.setVisibility(View.INVISIBLE);
        loadingTextView.setText("");
        loadingTextView.setVisibility(View.INVISIBLE);
        loadingProgressBar.setVisibility(View.VISIBLE);

        loadFacilities();
    }

    /**
     * Load the VA facilities
     */
    private void loadFacilities() {
        if(locationPermissionGranted())
            facilityLoader.loadPTSDPrograms();
        // Request the location permission before loading the facilities
        else
            requestLocationPermission();
    }

    /**
     * Remove the loading progress bar and TextView
     */
    private void hideLoadingBar() {
        if(loadingTextView != null) {
            loadingTextView.setVisibility(View.GONE);
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Close the on screen keyboard
     * See http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard/1109108#1109108
     */
    private void dismissKeyboard() {
        if(getActivity().getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }
    
}
