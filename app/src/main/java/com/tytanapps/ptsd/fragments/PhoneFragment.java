package com.tytanapps.ptsd.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.Utilities;


/**
 * Displays a list of common veteran hotlines. Shows a brief description for each hotline and
 * the phone number to call. Tapping on the hotline calls them.
 */
public class PhoneFragment extends AnalyticsFragment {

    private static final String LOG_TAG = PhoneFragment.class.getSimpleName();

    public PhoneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_phone, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_hotline).setChecked(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                insertDefaultPhoneNumbers();
                loadPhoneNumbersFromFirebase();
            }
        });
        t.run();
    }

    /**
     * Add the phone numbers that are to be used offline
     */
    private void insertDefaultPhoneNumbers() {
        View rootView = getView();
        if(rootView != null) {
            LinearLayout phoneNumbersLinearLayout = (LinearLayout) rootView.findViewById(R.id.phone_linear_layout);
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            insertPhoneCard(getString(R.string.veterans_crisis_line), getString(R.string.veterans_support_phone_details), getString(R.string.phone_veterans_crisis_line), inflater, phoneNumbersLinearLayout, R.drawable.veterans_crisis_line);
            insertPhoneCard(getString(R.string.lifeline_for_vets), getString(R.string.veterans_foundation_phone_details), getString(R.string.phone_veterans_foundation_hotline), inflater, phoneNumbersLinearLayout, R.drawable.nvf);
            insertPhoneCard(getString(R.string.suicice_lifeline), getString(R.string.suicide_lifeline_phone_details), getString(R.string.phone_suicide_lifeline), inflater, phoneNumbersLinearLayout, R.drawable.nspl);
            insertPhoneCard(getString(R.string.ncaad), getString(R.string.alcohol_phone_details), getString(R.string.phone_alcoholism), inflater, phoneNumbersLinearLayout, R.drawable.ncadd);
        }
    }

    /**
     * Load the list of phone numbers from a Firebase database
     */
    private void loadPhoneNumbersFromFirebase() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                readPhoneNumbers(database);
            }
        });
        t.start();
    }

    /**
     * Read the phone numbers from a Firebase database
     * @param database The database containing the phone number information
     */
    private void readPhoneNumbers(final FirebaseDatabase database) {
        DatabaseReference myRef = database.getReference("phone_support");

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
                            LinearLayout phoneNumbersLinearLayout = (LinearLayout) rootView.findViewById(R.id.phone_linear_layout);
                            LayoutInflater inflater = LayoutInflater.from(getActivity());

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                insertFirebasePhoneCard(child, phoneNumbersLinearLayout, inflater);
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
     * Add a phone card to the list
     * @param phoneDataSnapshot The DataSnapshot containing information about the phone number
     * @param phoneNumbersLinearLayout The linear layout to add the phone number to
     * @param inflater The layout inflater to inflate the card from xml
     */
    private void insertFirebasePhoneCard(final DataSnapshot phoneDataSnapshot, final LinearLayout phoneNumbersLinearLayout, final LayoutInflater inflater) {
        String name = (String) phoneDataSnapshot.child("name").getValue();
        String desc = (String) phoneDataSnapshot.child("description").getValue();
        String phone = (String) phoneDataSnapshot.child("phone_number").getValue();
        String bitmap_base64 = (String) phoneDataSnapshot.child("icon").getValue();

        byte[] imageAsBytes = Base64.decode(bitmap_base64, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

        insertPhoneCard(name, desc, phone, inflater, phoneNumbersLinearLayout, bmp);
    }

    /**
     * Insert a phone card
     * @param name The name of the phone hotline
     * @param desc The description of the phone number
     * @param phone The phone number
     * @param inflater The inflater to inflate the cardview with
     * @param phoneNumbersLinearLayout The linear layout containing the phone numbers
     * @param imageBitmap The logo as a bitmap
     */
    private void insertPhoneCard(String name, String desc, String phone, LayoutInflater inflater, LinearLayout phoneNumbersLinearLayout, Bitmap imageBitmap) {
        CardView phoneCardView = getPhoneCardView(inflater, phoneNumbersLinearLayout, name, phone);

        TextView descTextView = (TextView) phoneCardView.findViewById(R.id.phone_details_textview);
        descTextView.setText(desc);

        TextView phoneTextView = (TextView) phoneCardView.findViewById(R.id.phone_number_textview);
        phoneTextView.setText(phone);

        ImageView iconImageView = (ImageView) phoneCardView.findViewById(R.id.phone_icon_imageview);
        iconImageView.setImageBitmap(imageBitmap);
    }

    /**
     * Insert a phone card
     * @param name The name of the phone hotline
     * @param desc The description of the phone number
     * @param phone The phone number
     * @param inflater The inflater to inflate the cardview with
     * @param phoneNumbersLinearLayout The linear layout containing the phone numbers
     * @param imageResource The resource id of the logo drawable
     */
    private void insertPhoneCard(String name, String desc, String phone, LayoutInflater inflater, LinearLayout phoneNumbersLinearLayout, int imageResource) {
        CardView phoneCardView = getPhoneCardView(inflater, phoneNumbersLinearLayout, name, phone);

        TextView descTextView = (TextView) phoneCardView.findViewById(R.id.phone_details_textview);
        descTextView.setText(desc);

        TextView phoneTextView = (TextView) phoneCardView.findViewById(R.id.phone_number_textview);
        phoneTextView.setText(phone);

        ImageView iconImageView = (ImageView) phoneCardView.findViewById(R.id.phone_icon_imageview);
        iconImageView.setImageResource(imageResource);
    }

    /**
     * Get the card view representing a phone number. If it does not exist, create it and insert
     * it in the linear layout
     * @param inflater The layout inflater to create the phone card
     * @param phoneNumbersLinearLayout A linear layout containing all of the phone cards
     * @param name The name of the phone hotline
     * @param phoneNumber The phone number in the form #-###-###-###
     * @return The cardview containing the phone number information
     */
    private CardView getPhoneCardView(LayoutInflater inflater, LinearLayout phoneNumbersLinearLayout,
                                      String name, final String phoneNumber) {
        CardView phoneCardView;

        for(int i = 0; i < phoneNumbersLinearLayout.getChildCount(); i++) {
            View child = phoneNumbersLinearLayout.getChildAt(i);
            if(child.getTag() != null && child.getTag().equals(name))
                // Card has already been created
                return (CardView) child;
        }
        // Card has not yet been created
        phoneCardView = (CardView) inflater.inflate(R.layout.phone_cardview, (ViewGroup) getView(), false);

        phoneCardView.setTag(name);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.activity_vertical_margin));
        phoneCardView.setLayoutParams(params);

        phoneCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.openDialer(PhoneFragment.this, phoneNumber);
            }
        });

        phoneNumbersLinearLayout.addView(phoneCardView);
        Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_up);
        phoneCardView.startAnimation(bottomUp);

        return phoneCardView;
    }
}
