package com.tytanapps.ptsd.website;

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

import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

import static butterknife.ButterKnife.findById;
import static com.tytanapps.ptsd.utils.PtsdUtil.getLayoutChildren;
import static com.tytanapps.ptsd.utils.PtsdUtil.isVeteran;


/**
 * Displays a list of websites to find more information about PTSD. Shows a brief description for
 * each website. Tapping on the card opens the website.
 */
public class WebsiteFragment extends BaseFragment {

    @BindView(R.id.website_linear_layout) LinearLayout websitesLinearLayout;

    public WebsiteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                insertDefaultWebsites();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                readFirebaseWebsites(database);
            }
        }).start();
    }

    @Override
    protected int getNavigationItem() {
        return R.id.nav_websites;
    }

    @Override
    protected @StringRes int getTitle() {
        return R.string.web_title;
    }

    @Override
    public int getRootView() {
        return R.layout.fragment_website;
    }

    /**
     * Add the default websites to be used offline
     */
    private void insertDefaultWebsites() {
        View rootView = getView();
        if(rootView != null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            List<Website> websites = WebsiteDatabase.getWebsites(getActivity(), isVeteran(getActivity()));
            for (Website website : websites) {
                insertWebCard(website, inflater);
            }
        }
    }

    /**
     * Read websites from Firebase and add them to the list
     * @param database The firebase database containing the websites
     */
    private void readFirebaseWebsites(final FirebaseDatabase database) {
        DatabaseReference myRef = database.getReference("web_support");

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
                                boolean veteranOnly = snapshot.hasChild("veteranOnly") && snapshot.child("veteranOnly").getValue(Boolean.class);

                                if (!veteranOnly || isVeteran(getActivity())) {
                                    insertFirebaseWebCard(snapshot, inflater);
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

    /**
     * Add a website card to the list from Firebase
     * @param snapshot The data snapshot containing information about the website
     * @param inflater The inflater to inflate the card
     */
    private void insertFirebaseWebCard(final DataSnapshot snapshot, final LayoutInflater inflater) {
        Website website = snapshot.getValue(Website.class);
        insertWebCard(website, inflater);
    }

    private void insertWebCard(final Website website, final LayoutInflater inflater) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CardView webCardView = getWebCardView(website, inflater);

                TextView nameTextView = findById(webCardView, R.id.website_name_textview);
                nameTextView.setText(website.getName());

                TextView detailsTextView = findById(webCardView, R.id.website_details_textview);
                detailsTextView.setText(website.getDescription());

                final ImageView iconImageView = findById(webCardView, R.id.website_icon_imageview);

                if (website.getIconUrl() != null) {
                    Picasso.with(getActivity()).load(website.getIconUrl()).into(iconImageView);
                } else if (website.getIconRes() != 0) {
                    Picasso.with(getActivity()).load(website.getIconRes()).into(iconImageView);
                }
            }
        });
    }

    private CardView getWebCardView(final Website website, LayoutInflater inflater) {
        CardView websiteCardView = null;

        for (View child : getLayoutChildren(websitesLinearLayout)) {
            if (child.getTag() != null && child.getTag().equals(website.getName()))
                websiteCardView = (CardView) child;
        }
        if (websiteCardView == null) {
            websiteCardView = (CardView) inflater.inflate(R.layout.web_cardview, (ViewGroup) getView(), false);

            websiteCardView.setTag(website.getName());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.activity_vertical_margin));
            websiteCardView.setLayoutParams(params);

            websiteCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExternalAppUtil.openBrowserIntent(WebsiteFragment.this, website.getUrl());
                }
            });

            websitesLinearLayout.addView(websiteCardView);
            Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_to_top);
            websiteCardView.startAnimation(bottomUp);
        }

        return websiteCardView;
    }

}
