package com.tytanapps.ptsd.phone;

import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.CardView;
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
import com.tytanapps.ptsd.fragments.BaseFragment;
import com.tytanapps.ptsd.utils.ExternalAppUtil;

import butterknife.BindView;
import timber.log.Timber;

import static butterknife.ButterKnife.findById;
import static com.tytanapps.ptsd.utils.PtsdUtil.getLayoutChildren;
import static com.tytanapps.ptsd.utils.PtsdUtil.isVeteran;


/**
 * Displays a list of common veteran hotlines. Shows a brief description for each hotline and
 * the phone number to call. Tapping on the hotline calls them.
 */
public class PhoneFragment extends BaseFragment {

    @BindView(R.id.phone_linear_layout) LinearLayout phoneNumbersLinearLayout;

    public PhoneFragment() {
        // Required empty public constructor
    }

    public static PhoneFragment newInstance() {
        return new PhoneFragment();
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

    @Override
    protected @IdRes int getNavigationItem() {
        return R.id.nav_hotline;
    }

    @Override
    public int getRootView() {
        return R.layout.fragment_phone;
    }

    /**
     * Add the phone numbers that are to be used offline
     */
    private void insertDefaultPhoneNumbers() {
        View rootView = getView();
        if (rootView != null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            for (Phone phone : PhoneDatabase.getPhones(getActivity(), isVeteran(getActivity()))) {
                insertPhoneCard(phone, inflater);
            }
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
                        if (rootView != null) {
                            LayoutInflater inflater = LayoutInflater.from(getActivity());

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                boolean veteranOnly = snapshot.hasChild(getString(R.string.veteran_only)) && snapshot.child(getString(R.string.veteran_only)).getValue(Boolean.class);

                                if (!veteranOnly || isVeteran(getActivity())) {
                                    insertFirebasePhoneCard(snapshot, inflater);
                                }
                            }
                        }
                    }
                });

                t.run();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Timber.w(error.toException(), "Failed to read value.");
            }
        });
    }

    private void insertFirebasePhoneCard(final DataSnapshot phoneDataSnapshot, final LayoutInflater inflater) {
        Phone phone = phoneDataSnapshot.getValue(Phone.class);
        insertPhoneCard(phone, inflater);
    }

    private void insertPhoneCard(final Phone phone, final LayoutInflater inflater) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CardView phoneCardView = getPhoneCardView(phone, inflater);

                TextView descTextView = findById(phoneCardView, R.id.phone_details_textview);
                descTextView.setText(phone.getDescription());

                TextView phoneTextView = findById(phoneCardView, R.id.phone_number_textview);
                phoneTextView.setText(phone.getPhoneNumber());

                final ImageView iconImageView = findById(phoneCardView, R.id.phone_icon_imageview);

                if (phone.getIconUrl() != null) {
                    Picasso.with(getActivity()).load(phone.getIconUrl()).into(iconImageView);
                } else if (phone.getIconRes() != 0) {
                    Picasso.with(getActivity()).load(phone.getIconRes()).into(iconImageView);
                }
            }
        });

    }

    private CardView getPhoneCardView(final Phone phone, LayoutInflater inflater) {
        CardView phoneCardView;

        for (View child : getLayoutChildren(phoneNumbersLinearLayout)) {
            if (child.getTag() != null && child.getTag().equals(phone.getName()))
                // Card has already been created
                return (CardView) child;
        }

        // Card has not yet been created
        phoneCardView = (CardView) inflater.inflate(R.layout.phone_cardview, (ViewGroup) getView(), false);

        phoneCardView.setTag(phone.getName());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.activity_vertical_margin));
        //phoneCardView.setLayoutParams(params);

        phoneCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExternalAppUtil.openDialer(getActivity(), phone.getPhoneNumber());
            }
        });

        phoneNumbersLinearLayout.addView(phoneCardView);
        Animation bottomToTop = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_to_top);
        phoneCardView.startAnimation(bottomToTop);

        return phoneCardView;
    }
}
