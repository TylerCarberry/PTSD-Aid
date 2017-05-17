package com.tytanapps.ptsd.facility;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
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
import android.widget.Button;
import android.widget.TextView;

import com.tytanapps.ptsd.LocationNotFoundException;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.firebase.RemoteConfig;
import com.tytanapps.ptsd.fragments.BaseFragment;
import com.tytanapps.ptsd.utils.PermissionUtil;
import com.tytanapps.ptsd.utils.PtsdUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tytanapps.ptsd.utils.PermissionUtil.REQUEST_LOCATION_PERMISSION;

/**
 * Loads a list of nearby VA facilities that offer PTSD programs.
 * Displays the address, phone number, programs, and an image for each facility.
 * You can call them, get directions, or load the website for each VA facility.
 */
public class FacilitiesFragment extends BaseFragment {

    private FacilityLoader facilityLoader;
    private final List<Facility> facilityList = new ArrayList<>();
    private FacilityAdapter mAdapter;

    @Inject RemoteConfig remoteConfig;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.facility_loading_textview) TextView loadingTextView;
    @BindView(R.id.facility_progressbar) View loadingProgressBar;
    @BindView(R.id.retry_load_button) Button retryLoadButton;

    public FacilitiesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getApplication().getPtsdComponent().inject(this);
        super.onCreate(savedInstanceState);

        facilityLoader = new FacilityLoader(this) {
            @Override
            public void errorLoadingResults(Throwable throwable) {
                FacilitiesFragment.this.errorLoadingResults(throwable);
            }

            @Override
            public void onSuccess(List<Facility> loadedFacilities) {
                FacilitiesFragment.this.onAllFacilitiesLoaded(loadedFacilities);
            }

            @Override
            public void onLoadedImage(int facilityId) {}
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_nearby_facilities, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setEnabled(false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Load the VA facilities if they have not yet been loaded
        if (facilityList.isEmpty()) {
            loadVaFacilities();
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
                if (mAdapter != null) {
                    mAdapter.filter(query);
                }
                scrollFacilityListToTop();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mAdapter != null) {
                    mAdapter.filter(newText);
                }
                scrollFacilityListToTop();
                return true;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        PtsdUtil.dismissKeyboard(getActivity());
    }

    @Override
    protected int getNavigationItem() {
        return R.id.nav_facilities;
    }

    @Override
    protected @StringRes int getTitle() {
        return R.string.facilities_title;
    }

    /**
     * Scroll the facility recycler view to the top
     */
    private void scrollFacilityListToTop() {
        recyclerView.scrollToPosition(0);
    }

    /**
     * Setup the RecyclerView and link it to the FacilityAdapter
     */
    private void setupRecyclerView() {
        mAdapter = new FacilityAdapter(facilityList, this, remoteConfig.getInt(R.string.rc_facilities_to_display));
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
    private void onAllFacilitiesLoaded(final List<Facility> facilities) {
        hideLoadingBar();

        facilityList.clear();
        facilityList.addAll(facilities);

        setupRecyclerView();
        enableRefreshLayout();

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Enable the refresh layout and stop the refreshing animation
     */
    private void enableRefreshLayout() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                if (PermissionUtil.locationPermissionGranted(getActivity())) {
                    facilityLoader.loadPTSDPrograms();
                } else {
                    errorLoadingResults(getString(R.string.error_location_permission));
                }
                break;
            }
        }
    }

    /**
     * There was an error loading the VA facilities. Display an error message
     */
    private void errorLoadingResults(Throwable throwable) {
        if (throwable instanceof LocationNotFoundException) {
            errorLoadingResults(getString(R.string.gps_error));
        } else {
            errorLoadingResults(getString(R.string.va_loading_error));
        }
    }

    /**
     * There was an error loading the VA facilities. Display a message and a retry button.
     * @param errorMessage The message to show to the user
     */
    private void errorLoadingResults(String errorMessage) {
        if (getView() != null) {
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

        loadVaFacilities();
    }

    private void loadVaFacilities() {
        if (PermissionUtil.locationPermissionGranted(getActivity())) {
            facilityLoader.loadPTSDPrograms();
        } else {
            PermissionUtil.requestLocationPermission(getActivity());
        }
    }

    /**
     * Remove the loading progress bar and TextView
     */
    private void hideLoadingBar() {
        if (loadingTextView != null) {
            loadingTextView.setVisibility(View.GONE);
            loadingProgressBar.setVisibility(View.GONE);
        }
    }
    
}
