package com.tytanapps.ptsd.fragments;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.widget.CardView;
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
import com.squareup.picasso.Picasso;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.utils.ExternalAppUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

import static butterknife.ButterKnife.findById;
import static com.tytanapps.ptsd.utils.PtsdUtil.isVeteran;


/**
 * Displays a list of common veteran hotlines. Shows a brief description for each hotline and
 * the phone number to call. Tapping on the hotline calls them.
 */
public class PhoneFragment extends BaseFragment {

    private static final String LOG_TAG = PhoneFragment.class.getSimpleName();

    @BindView(R.id.phone_linear_layout) LinearLayout phoneNumbersLinearLayout;

    public PhoneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_phone, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setCheckedNavigationItem(R.id.nav_hotline);
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

    @Override
    protected @StringRes int getTitle() {
        return R.string.phone_title;
    }

    /**
     * Add the phone numbers that are to be used offline
     */
    private void insertDefaultPhoneNumbers() {
        View rootView = getView();
        if(rootView != null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            if(isVeteran(getActivity())) {
                insertPhoneCard(getString(R.string.veterans_crisis_line), getString(R.string.veterans_support_phone_details), getString(R.string.phone_veterans_crisis_line), inflater, phoneNumbersLinearLayout, R.drawable.veterans_crisis_line);
                insertPhoneCard(getString(R.string.lifeline_for_vets), getString(R.string.veterans_foundation_phone_details), getString(R.string.phone_veterans_foundation_hotline), inflater, phoneNumbersLinearLayout, R.drawable.nvf);
            }
            insertPhoneCard(getString(R.string.suicide_lifeline), getString(R.string.suicide_lifeline_phone_details), getString(R.string.phone_suicide_lifeline), inflater, phoneNumbersLinearLayout, R.drawable.nspl);
            insertPhoneCard(getString(R.string.ncaad), getString(R.string.alcohol_phone_details), getString(R.string.phone_alcoholism), inflater, phoneNumbersLinearLayout, R.drawable.ncadd);
        }
    }

    /**
     * Load the list of phone numbers from a Firebase database
     */
    private void loadPhoneNumbersFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        readPhoneNumbers(database);
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
                            LayoutInflater inflater = LayoutInflater.from(getActivity());

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                boolean veteranOnly = snapshot.hasChild("veteran_only") && snapshot.child("veteran_only").getValue(Boolean.class);

                                if(!veteranOnly || isVeteran(getActivity())) {
                                    insertFirebasePhoneCard(snapshot, phoneNumbersLinearLayout, inflater);
                                }
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
        String iconUrl = (String) phoneDataSnapshot.child("icon_url").getValue();

        insertPhoneCard(name, desc, phone, inflater, phoneNumbersLinearLayout, iconUrl);
    }

    /**
     * Insert a phone card
     * @param name The name of the phone hotline
     * @param desc The description of the phone number
     * @param phone The phone number
     * @param inflater The inflater to inflate the cardview with
     * @param phoneNumbersLinearLayout The linear layout containing the phone numbers
     * @param iconUrl The url of the icon
     */
    private void insertPhoneCard(String name, String desc, String phone, LayoutInflater inflater, LinearLayout phoneNumbersLinearLayout, String iconUrl) {
        CardView phoneCardView = getPhoneCardView(inflater, phoneNumbersLinearLayout, name, phone);

        TextView descTextView = findById(phoneCardView, R.id.phone_details_textview);
        descTextView.setText(desc);

        TextView phoneTextView = findById(phoneCardView, R.id.phone_number_textview);
        phoneTextView.setText(phone);

        ImageView iconImageView = findById(phoneCardView, R.id.phone_icon_imageview);
        Picasso.with(getActivity()).load(iconUrl).into(iconImageView);
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

        TextView descTextView = findById(phoneCardView, R.id.phone_details_textview);
        descTextView.setText(desc);

        TextView phoneTextView = findById(phoneCardView, R.id.phone_number_textview);
        phoneTextView.setText(phone);

        ImageView iconImageView = findById(phoneCardView, R.id.phone_icon_imageview);
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
                ExternalAppUtil.openDialer(getActivity(), phoneNumber);
            }
        });

        phoneNumbersLinearLayout.addView(phoneCardView);
        Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_up);
        phoneCardView.startAnimation(bottomUp);

        return phoneCardView;
    }
}
