package com.tytanapps.ptsd.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tytanapps.ptsd.Facility;
import com.tytanapps.ptsd.FacilityAdapter;
import com.tytanapps.ptsd.FacilityLoader;
import com.tytanapps.ptsd.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads a list of nearby VA facilities that offer PTSD programs.
 * Displays the address, phone number, programs, and an image for each facility.
 * You can call them, get directions, or load the website for each VA facility.
 */
public class FacilitiesFragment extends AnalyticsFragment {

    private static final String LOG_TAG = FacilitiesFragment.class.getSimpleName();

    private static final int PERMISSION_LOCATION_REQUEST = 3;

    private FacilityLoader facilityLoader;

    private List<Facility> facilityList = new ArrayList<>();
    private FacilityAdapter mAdapter;

    public FacilitiesFragment() {
        // Required default constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        facilityLoader = new FacilityLoader(this) {
            @Override
            public void errorLoadingResults(String errorMessage) {
                FacilitiesFragment.this.errorLoadingResults();
            }

            @Override
            public void onSuccess(List<Facility> loadedFacilities) {
                FacilitiesFragment.this.allFacilitiesHaveLoaded(loadedFacilities);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
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

        // Load the VA facilities if they have not yet been loaded
        if(facilityList.size() == 0) {
            if (locationPermissionGranted())
                facilityLoader.loadPTSDPrograms();
            else
                requestLocationPermission();
        }
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
                scrollListToTop();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mAdapter != null)
                    mAdapter.filter(newText);
                scrollListToTop();
                return true;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissKeyboard();
    }

    private void scrollListToTop() {
        View rootView = getView();
        if(rootView != null) {
            RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
            recyclerView.scrollToPosition(0);
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
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

            mAdapter = new FacilityAdapter(facilityList, this);
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
                facilityList.clear();
                mAdapter.notifyDataSetChanged();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        facilityLoader.refresh();
                    }
                });
                t.run();
            }
        });
        swipeRefreshLayout.setEnabled(false);
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
        View rootView = getView();
        if(rootView != null) {
            SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

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

                        facilityLoader.loadPTSDPrograms();
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

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }
    
}
