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
 * Displays a list of websites to find more information about PTSD. Shows a brief description for
 * each website. Tapping on the card opens the website.
 */
public class WebsiteFragment extends BaseFragment {

    private static final String LOG_TAG = WebsiteFragment.class.getSimpleName();

    @BindView(R.id.website_linear_layout) LinearLayout websitesLinearLayout;

    public WebsiteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_website, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
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

    /**
     * Add the default websites to be used offline
     */
    private void insertDefaultWebsites() {
        View rootView = getView();
        if(rootView != null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());

            if(isVeteran(getActivity())) {
                insertWebCard(getString(R.string.veterans_chat_title), getString(R.string.veterans_chat_details), getString(R.string.website_chat), R.drawable.veterans_crisis_line, inflater, websitesLinearLayout);
                insertWebCard(getString(R.string.veterans_affairs), getString(R.string.veteran_affairs_details), getString(R.string.website_va), R.drawable.va, inflater, websitesLinearLayout);
                insertWebCard(getString(R.string.self_quiz), getString(R.string.quiz_details), getString(R.string.website_self_check), R.drawable.veterans_quiz, inflater, websitesLinearLayout);
            }
            insertWebCard(getString(R.string.nimh_title), getString(R.string.nimh_details), getString(R.string.website_nimh), R.drawable.nimh, inflater, websitesLinearLayout);
            insertWebCard(getString(R.string.ptsd_coach), getString(R.string.ptsd_coach_details), getString(R.string.website_coach), R.drawable.ptsd_coach, inflater, websitesLinearLayout);
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
                                boolean veteranOnly = snapshot.hasChild("veteran_only") && snapshot.child("veteran_only").getValue(Boolean.class);

                                if(!veteranOnly || isVeteran(getActivity())) {
                                    insertFirebaseWebCard(snapshot, websitesLinearLayout, inflater);
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
     * Add a website card to the list from Firebase
     * @param snapshot The data snapshot containing information about the website
     * @param websitesLinearLayout The linear layout to add the website to
     * @param inflater The inflater to inflate the card
     */
    private void insertFirebaseWebCard(final DataSnapshot snapshot, final LinearLayout websitesLinearLayout, final LayoutInflater inflater) {
        String name = (String) snapshot.child("name").getValue();
        String desc = (String) snapshot.child("description").getValue();
        String url = (String) snapshot.child("url").getValue();
        String icon_url = (String) snapshot.child("icon_url").getValue();

        insertWebCard(name, desc, url, icon_url, inflater, websitesLinearLayout);
    }

    /**
     * Add a website card to the list
     * @param name The name of the website
     * @param desc The description of the website
     * @param url The url of the website
     * @param icon_url The url of the icon resource
     * @param inflater The inflater to inflate the card
     * @param websitesLinearLayout The linear layout to add the website to
     */
    private void insertWebCard(String name, String desc, String url, String icon_url, LayoutInflater inflater, LinearLayout websitesLinearLayout) {
        CardView webCardView = getWebCardView(inflater, websitesLinearLayout, name, url);

        TextView nameTextView = findById(webCardView, R.id.website_name_textview);
        nameTextView.setText(name);

        TextView detailsTextView = findById(webCardView, R.id.website_details_textview);
        detailsTextView.setText(desc);

        ImageView iconImageView = findById(webCardView, R.id.website_icon_imageview);
        Picasso.with(getActivity()).load(icon_url).into(iconImageView);
    }

    /**
     * Add a website card to the list
     * @param name The name of the website
     * @param desc The description of the website
     * @param url The url of the website
     * @param imageResource The resource id of the logo drawable
     * @param inflater The inflater to inflate the card
     * @param websitesLinearLayout The linear layout to add the website to
     */
    private void insertWebCard(final String name, final String desc, final String url, final int imageResource, final LayoutInflater inflater, final LinearLayout websitesLinearLayout) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CardView webCardView = getWebCardView(inflater, websitesLinearLayout, name, url);

                TextView nameTextView = findById(webCardView, R.id.website_name_textview);
                nameTextView.setText(name);

                TextView detailsTextView = findById(webCardView, R.id.website_details_textview);
                detailsTextView.setText(desc);

                ImageView iconImageView = findById(webCardView, R.id.website_icon_imageview);
                iconImageView.setImageResource(imageResource);
            }
        });
    }

    private CardView getWebCardView(LayoutInflater inflater, LinearLayout websitesLinearLayout, String name, final String url) {
        CardView websiteCardView = null;

        for(int i = 0; i < websitesLinearLayout.getChildCount(); i++) {
            View child = websitesLinearLayout.getChildAt(i);
            if(child.getTag() != null && child.getTag().equals(name))
                websiteCardView = (CardView) child;
        }
        if(websiteCardView == null) {
            websiteCardView = (CardView) inflater.inflate(R.layout.web_cardview, (ViewGroup) getView(), false);

            websiteCardView.setTag(name);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.activity_vertical_margin));
            websiteCardView.setLayoutParams(params);

            websiteCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExternalAppUtil.openBrowserIntent(WebsiteFragment.this, url);
                }
            });

            websitesLinearLayout.addView(websiteCardView);
            Animation bottomUp = AnimationUtils.loadAnimation(getActivity(), R.anim.bottom_to_top);
            websiteCardView.startAnimation(bottomUp);
        }

        return websiteCardView;
    }

}
