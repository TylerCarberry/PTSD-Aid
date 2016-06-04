package com.tytanapps.ptsd.fragments;

import android.app.Fragment;
import android.os.Bundle;
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
import com.tytanapps.ptsd.R;


/**
 * Shows information about PTSD. Gives symptoms, causes, and treatment for PTSD.
 */
public class ResourcesFragment extends Fragment {

    private static final String LOG_TAG = ResourcesFragment.class.getSimpleName();

    public ResourcesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_resources, container, false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        readResources(database);

        return rootView;
    }

    private void readResources(final FirebaseDatabase database) {
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
                        if(rootView != null) {

                            LinearLayout phoneNumbersLinearLayout = (LinearLayout) rootView.findViewById(R.id.resources_linear_layout);
                            LayoutInflater inflater = LayoutInflater.from(getActivity());

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                insertResource(child, phoneNumbersLinearLayout, inflater);
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

    private void insertResource(final DataSnapshot phoneDataSnapshot, final LinearLayout phoneNumbersLinearLayout, final LayoutInflater inflater) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String title = (String) phoneDataSnapshot.child("title").getValue();
                String desc = (String) phoneDataSnapshot.child("description").getValue();

                LinearLayout resourceHeaderView = getResourceView(inflater, phoneNumbersLinearLayout, title);

                TextView headerTextView = (TextView) resourceHeaderView.findViewById(R.id.resource_header);
                headerTextView.setText(title);

                TextView descTextView = (TextView) resourceHeaderView.findViewById(R.id.resource_desc);
                descTextView.setText(desc);
            }
        });

        t.run();
    }

    private LinearLayout getResourceView(LayoutInflater inflater, LinearLayout resourcesLinearLayout, String name) {
        Log.d(LOG_TAG, "getResourceView() called with: " + "inflater = [" + inflater + "], resourcesLinearLayout = [" + resourcesLinearLayout + "], name = [" + name + "]");

        LinearLayout resourceView = null;

        for(int i = 0; i < resourcesLinearLayout.getChildCount(); i++) {
            View child = resourcesLinearLayout.getChildAt(i);

            if(child.getTag() != null && child.getTag().equals(name))
                resourceView = (LinearLayout) child;
        }
        if(resourceView == null) {
            resourceView = (LinearLayout) inflater.inflate(R.layout.resource_item, (ViewGroup) getView(), false);

            resourceView.setTag(name);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.activity_vertical_margin));
            resourceView.setLayoutParams(params);

            resourcesLinearLayout.addView(resourceView);
        }

        return resourceView;
    }

}
