package com.tytanapps.ptsd.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tytanapps.ptsd.PTSDApplication;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.firebase.RemoteConfig;

import javax.inject.Inject;


/**
 * Shows information about PTSD. Gives symptoms, causes, and treatment for PTSD.
 */
public class ResourcesFragment extends BaseFragment {

    private static final String LOG_TAG = ResourcesFragment.class.getSimpleName();

    @Inject RemoteConfig remoteConfig;

    public ResourcesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ((PTSDApplication)getActivity().getApplication()).getFirebaseComponent().inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_resources, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                insertDefaultResources();
                readResourcesFromFirebase(FirebaseDatabase.getInstance());
            }
        }).run();

        setCheckedNavigationItem(R.id.nav_resources);
    }

    @Override
    protected @StringRes int getTitle() {
        return R.string.resources_title;
    }

    private void insertDefaultResources() {
        View rootView = getView();
        if (rootView != null) {
            LinearLayout phoneNumbersLinearLayout = (LinearLayout) rootView.findViewById(R.id.resources_linear_layout);
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            insertResource(getString(R.string.symptoms), getString(R.string.symptoms_detail), inflater, phoneNumbersLinearLayout);
            insertResource(getString(R.string.causes), getString(R.string.causes_detail), inflater, phoneNumbersLinearLayout);
            insertResource(getString(R.string.diagnosis), getString(R.string.diagnosis_detail), inflater, phoneNumbersLinearLayout);
            insertResource(getString(R.string.treatment), getString(R.string.treatment_detail), inflater, phoneNumbersLinearLayout);
            insertResource(getString(R.string.counseling), getString(R.string.counseling_detail), inflater, phoneNumbersLinearLayout);
        }
    }

    /**
     * Read the resources from the firebase database and add them to the list
     * @param database The database to read from
     */
    private void readResourcesFromFirebase(final FirebaseDatabase database) {
        DatabaseReference myRef = database.getReference("resources");

        // Read from the database
        myRef.orderByChild("order").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        View rootView = getView();
                        if (rootView != null) {
                            LinearLayout phoneNumbersLinearLayout = (LinearLayout) rootView.findViewById(R.id.resources_linear_layout);
                            LayoutInflater inflater = LayoutInflater.from(getActivity());

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                insertFirebaseResource(child, phoneNumbersLinearLayout, inflater);
                            }
                        }
                    }
                });

                t.run();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(LOG_TAG, "Failed to read value.", error.toException());
            }
        });
    }

    /**
     * Add a resource to the list
     * @param resourceDataSnapshot The data snapshot containing information about the resource
     * @param resourcesLinearLayout The linearlayout to add the info to
     * @param inflater The layout inflater to create the view from xml
     */
    private void insertFirebaseResource(final DataSnapshot resourceDataSnapshot, final LinearLayout resourcesLinearLayout, final LayoutInflater inflater) {
        String title = (String) resourceDataSnapshot.child("title").getValue();
        String desc = (String) resourceDataSnapshot.child("description").getValue();

        insertResource(title, desc, inflater, resourcesLinearLayout);
    }

    /**
     * Insert a resource
     * @param title The title of the resource
     * @param desc The information about the resource
     * @param inflater The inflater to inflate the layouts
     * @param resourcesLinearLayout The linear layout to add the resource to
     */
    private void insertResource(String title, String desc, LayoutInflater inflater, LinearLayout resourcesLinearLayout) {
        LinearLayout resourceHeaderView = getResourceView(inflater, resourcesLinearLayout, title);

        TextView headerTextView = (TextView) resourceHeaderView.findViewById(R.id.resource_header);
        headerTextView.setText(title);
        if (remoteConfig.getBoolean(getActivity(), R.string.rc_resource_sticky)) {
            headerTextView.setTag("sticky");
        }

        TextView descTextView = (TextView) resourceHeaderView.findViewById(R.id.resource_desc);
        descTextView.setText(desc);
    }

    /**
     * Get the view with information about the resource. Create it and add it to
     * the list if it doesn't exist
     * @param inflater The layout inflater to create the view from xml
     * @param resourcesLinearLayout The linear layout containing the resources
     * @param name The name of the resource to find
     * @return The Linear Layout containing information about the resource
     */
    private LinearLayout getResourceView(LayoutInflater inflater, LinearLayout resourcesLinearLayout, String name) {
        // Find the resource view if it exists
        for (int i = 0; i < resourcesLinearLayout.getChildCount(); i++) {
            View child = resourcesLinearLayout.getChildAt(i);

            if (child.getTag() != null && child.getTag().equals(name))
                return (LinearLayout) child;
        }

        // Resource view does not exist, make it
        LinearLayout resourceView = (LinearLayout) inflater.inflate(R.layout.resource_item, (ViewGroup) getView(), false);
        resourceView.setTag(name);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // width
                LinearLayout.LayoutParams.WRAP_CONTENT  // height
        );

        params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.activity_vertical_margin));
        resourceView.setLayoutParams(params);
        resourcesLinearLayout.addView(resourceView);

        return resourceView;
    }

}
