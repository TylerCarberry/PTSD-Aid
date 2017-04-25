package com.tytanapps.ptsd.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tytanapps.ptsd.MainActivity;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.facility.FacilitiesFragment;
import com.tytanapps.ptsd.firebase.RemoteConfig;
import com.tytanapps.ptsd.utils.ExternalAppUtil;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * The main fragment displayed when you launch the app. Prompts the user for their emotion
 * and gives them recommendations based on their answer.
 */
public class MainFragment extends BaseFragment {
    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    private boolean firebaseDatabaseLoaded = false;

    @BindView(R.id.recommendations_linear_layout) LinearLayout recommendationsLinearLayout;
    @BindView(R.id.recommendations_container) FrameLayout recommendationsContainer;
    @BindView(R.id.main_header_text_view) TextView headerTextView;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the Firebase database is loaded, set the field firebaseDatabaseLoaded to true
        determineIfFirebaseDatabaseLoaded();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setupEmotions(rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Hide the sign in button if the user is already signed in
        if(isUserSignedIn())
            hideSignInButton();

        setCheckedNavigationItem(R.id.nav_recommendations);
    }

    @Override
    protected @StringRes int getTitle() {
        return R.string.recommendations_title;
    }

    /**
     * Add listeners to the emoji buttons to show the suggestions when tapped
     * Hides the extra emoji if show_extra_emoji is false on remote config
     * @param rootView The root view of the fragment, containing the emotion buttons
     */
    private void setupEmotions(View rootView) {
        if(!RemoteConfig.getBoolean(this, R.string.rc_show_extra_emoji)) {
            rootView.findViewById(R.id.emotions2_linear_layout).setVisibility(View.GONE);
        }
    }

    /**
     * Hide the sign in button on the navigation drawer
     */
    private void hideSignInButton() {
        View rootView = getView();
        if(rootView != null) {
            View signInButton = rootView.findViewById(R.id.button_sign_in);
            if (signInButton != null)
                signInButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Determine whether the user is signed in to their Google account
     * Precondition: MainFragment is contained in of MainActivity
     * @return Whether the user is signed in
     */
    private boolean isUserSignedIn() {
        return ((MainActivity) getActivity()).isUserSignedIn();
    }

    /**
     * If the Firebase database is loaded, set firebaseDatabaseLoaded to true
     */
    private void determineIfFirebaseDatabaseLoaded() {

        // Attempt to load a value from the database. If it cannot be loaded, then the listener
        // will never be called and firebaseDatabaseLoaded will remain false
        FirebaseDatabase myRef = FirebaseDatabase.getInstance();
        myRef.getReference("recommendations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firebaseDatabaseLoaded = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                firebaseDatabaseLoaded = false;
            }
        });
    }

    /**
     * When an emotion icon is tapped, show the corresponding recommendations and hide the other icons
     * @param emotionPressed The emotion icon that was tapped
     */
    @OnClick({R.id.happy_face, R.id.ok_face, R.id.sad_face, R.id.sick_face, R.id.poop_emoji})
    public void emotionSelected(final View emotionPressed) {
        View fragmentView = getView();
        if(fragmentView != null) {
            recommendationsContainer.setVisibility(View.INVISIBLE);

            recommendationsLinearLayout.removeAllViews();

            // Remove on click listener from the emoji
            emotionPressed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {}
            });

            String emotionName = "";
            // Show the suggestions for each emotion
            switch (emotionPressed.getId()) {
                case R.id.happy_face:
                    emotionName = "happy";
                    recommendationsLinearLayout.addView(getSuggestionVAWebsite());
                    recommendationsLinearLayout.addView(getSuggestionVisitResources());

                    int newestAppVersion = RemoteConfig.getInt(this, R.string.rc_newest_app_version);
                    int currentAppVersion = ExternalAppUtil.getApkVersion(getActivity());
                    if (newestAppVersion > 0 && currentAppVersion > 0 && newestAppVersion > currentAppVersion) {
                        recommendationsLinearLayout.addView(getSuggestionUpdateApp());
                    }

                    break;

                case R.id.ok_face:
                    emotionName = "ok";
                    recommendationsLinearLayout.addView(getSuggestionVisitResources());
                    recommendationsLinearLayout.addView(getSuggestionJoinVeteranAssociation());
                    break;

                case R.id.sad_face:
                    emotionName = "sad";
                    if (isTrustedContactCreated())
                        recommendationsLinearLayout.addView(getSuggestionCallTrustedContact());
                    recommendationsLinearLayout.addView(getSuggestionJoinVeteranAssociation());
                    recommendationsLinearLayout.addView(getSuggestionCallVeteranFoundation());
                    break;

                case R.id.sick_face:
                    emotionName = "sick";
                    recommendationsLinearLayout.addView(getSuggestionFindFacility());
                    if (isTrustedContactCreated())
                        recommendationsLinearLayout.addView(getSuggestionCallTrustedContact());
                    recommendationsLinearLayout.addView(getSuggestionVisitResources());
                    break;

                case R.id.poop_emoji:
                    emotionName = "poop";
                    if (isTrustedContactCreated())
                        recommendationsLinearLayout.addView(getSuggestionCallTrustedContact());
                    recommendationsLinearLayout.addView(getSuggestionCallVeteransCrisisLine());
                    recommendationsLinearLayout.addView(getSuggestionJoinVeteranAssociation());
                    break;
            }

            if (!isTrustedContactCreated())
                recommendationsLinearLayout.addView(getSuggestionAddTrustedContact());

            if(firebaseDatabaseLoaded && RemoteConfig.getBoolean(this, R.string.rc_check_recommendations_database)) {
                getRecommendationsFromDatabase(FirebaseDatabase.getInstance(), emotionName, emotionPressed.getId());
            }
            else {
                fadeOutAllEmojiExcept(emotionPressed.getId());
                animateOutEmotionPrompt();
                animateInRecommendations(recommendationsContainer);
            }
        }
    }

    /**
     * Read the phone numbers from a Firebase database
     * @param database The database containing the phone number information
     * @param id The id of the
     */
    private void getRecommendationsFromDatabase(final FirebaseDatabase database, String emotion, final int id) {
        DatabaseReference myRef = database.getReference("recommendations").child(emotion);

        // Read from the database
        myRef.orderByChild("order").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                final Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        View rootView = getView();
                        if(rootView != null) {
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                recommendationsLinearLayout.addView(getSuggestionFromDatabase(child));
                            }

                            animateOutEmotionPrompt();
                            fadeOutAllEmojiExcept(id);

                            animateInRecommendations(recommendationsContainer);
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
     * Get a blank suggestion layout
     * @return A blank suggestion layout
     */
    private RelativeLayout getSuggestionLayoutTemplate() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        return (RelativeLayout) inflater.inflate(R.layout.recommendation_view, getRootViewGroup(), false);
    }

    /**
     * Get the recommendation to create a trusted contact
     * When tapped, show the create trusted contact dialog
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionAddTrustedContact() {
       return createSuggestionLayout(getString(R.string.recommendation_add_trusted_contact), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).showCreateTrustedContactDialog();
            }
        });
    }

    /**
     * Get the recommendation to sign in to your Google Account
     * When tapped, open the sign in dialog
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionSignIn() {
        return createSuggestionLayout(getString(R.string.recommendation_sign_in), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
            }
        });
    }

    /**
     * Get the recommendation to visit the VA website
     * When tapped, open the VA website
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionVAWebsite() {
        return createSuggestionLayout(getString(R.string.recommendation_veteran_benefits), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExternalAppUtil.openBrowserIntent(MainFragment.this, (getString(R.string.website_va)));
            }
        });
    }

    /**
     * Get the recommendation to join a veteran association
     * When tapped, open the veterans network website
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionJoinVeteranAssociation() {
        return createSuggestionLayout(getString(R.string.recommendation_veteran_association), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExternalAppUtil.openBrowserIntent(MainFragment.this, (getString(R.string.veterans_network_website)));
            }
        });
    }

    /**
     * Get the recommendation to call your trusted contact
     * When tapped, call your trusted contact if it exists. Create one if it doesn't
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionCallTrustedContact() {
        return createSuggestionLayout(getString(R.string.recommendation_call_trusted_contact), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "");
                if (!phoneNumber.equals(""))
                    ExternalAppUtil.openDialer(getActivity(), phoneNumber);
                else
                    ((MainActivity) getActivity()).showCreateTrustedContactDialog();
            }
        });
    }

    /**
     * Get the recommendation to call the suicide lifeline
     * When tapped, call the suicide lifeline
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionCallVeteransCrisisLine() {
        return createSuggestionLayout(getString(R.string.recommendation_call_veterans_crisis_line), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = getSharedPreferenceString(getString(R.string.phone_suicide_lifeline), "");
                ExternalAppUtil.openDialer(getActivity(), phoneNumber);
            }
        });
    }

    /**
     * Get the recommendation to call the Veteran Foundation
     * When tapped, call the veteran foundation
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionCallVeteranFoundation() {
        return createSuggestionLayout(getString(R.string.recommendation_call_veterans_foundation), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = getSharedPreferenceString(getString(R.string.phone_veterans_foundation_hotline), "");
                ExternalAppUtil.openDialer(getActivity(), phoneNumber);
            }
        });
    }

    private RelativeLayout getSuggestionFromDatabase(DataSnapshot dataSnapshot) {
        String message = (String) dataSnapshot.child("text").getValue();

        try {
            final String url = (String) dataSnapshot.child("url").getValue();

            if (url != null && url.length() > 0) {
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ExternalAppUtil.openBrowserIntent(MainFragment.this, url);
                    }
                };
                return createSuggestionLayout(message, onClickListener);
            }
        // If an exception is thrown, there is no phone number associated with the recommendation
        } catch (Exception ignored) {}


        // If an exception is thrown, there is no website associated with the recommendation
        try {
            final String phoneNumber = (String) dataSnapshot.child("phone_number").getValue();

            if (phoneNumber != null && phoneNumber.length() > 0) {
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ExternalAppUtil.openDialer(getActivity(), phoneNumber);
                    }
                };
                return createSuggestionLayout(message, onClickListener);
            }

        } catch (Exception ignored) {}

        // If an exception is thrown, there is no website associated with the recommendation
        try {
            final String location = (String) dataSnapshot.child("map").getValue();

            if (location != null && location.length() > 0) {
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            ExternalAppUtil.openMapIntent(MainFragment.this, ExternalAppUtil.getMapUri(location));
                        } catch (UnsupportedEncodingException e) {
                            FirebaseCrash.report(e);
                            e.printStackTrace();
                        }
                    }
                };
                return createSuggestionLayout(message, onClickListener);
            }

        } catch (Exception e) {
            FirebaseCrash.report(e);
        }


        // There is no website or phone number associated with the recommendation
        return createSuggestionLayout(message, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do nothing when tapped
            }
        });
    }

    /**
     * Get the recommendation to look at the resources
     * When tapped, open the drawer
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionVisitResources() {
        return createSuggestionLayout(getString(R.string.recommendation_resources), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((MainActivity)getActivity()).switchFragment(new ResourcesFragment());
                } catch(Exception e) {
                    FirebaseCrash.report(e);
                    openDrawer();
                }
            }
        });

    }

    /**
     * Get the recommendation to look at the resources
     * When tapped, open the drawer
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionFindFacility() {
        return createSuggestionLayout(getString(R.string.recommendation_find_facility), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((MainActivity)getActivity()).switchFragment(new FacilitiesFragment());
                } catch(Exception e) {
                    FirebaseCrash.report(e);
                    openDrawer();
                }
            }
        });

    }

    /**
     * Get the recommendation to look at the resources
     * When tapped, open the drawer
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionUpdateApp() {
        return createSuggestionLayout(getString(R.string.recommendation_update_app), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ExternalAppUtil.openBrowserIntent(MainFragment.this, "https://play.google.com/store/apps/details?id=com.tytanapps.ptsd");
                } catch(Exception e) {
                    FirebaseCrash.report(e);
                    openDrawer();
                }
            }
        });

    }

    /**
     * Create a suggestion layout to show the user
     * @param text The message to show in the recommendation
     * @param onClickListener The action to perform when the recommendation is tapped
     * @return The suggestion layout
     */
    private RelativeLayout createSuggestionLayout(String text, View.OnClickListener onClickListener) {
        RelativeLayout suggestionLayout = getSuggestionLayoutTemplate();
        TextView signInTextView = (TextView) suggestionLayout.findViewById(R.id.recommendation_textview);
        signInTextView.setText(text);
        suggestionLayout.setOnClickListener(onClickListener);

        return suggestionLayout;
    }

    /**
     * @return Whether the trusted contact has been created
     */
    private boolean isTrustedContactCreated() {
        String trustedContactPhone = getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "");
        return !trustedContactPhone.equals("");
    }

    /**
     * Fade out and remove all emoji except for one
     * @param emoji_id The id of the view to not remove
     */
    private void fadeOutAllEmojiExcept(int emoji_id) {
        View rootView = getView();
        if(rootView != null) {

            LinearLayout emojiLayout1 = (LinearLayout) rootView.findViewById(R.id.emotions_linear_layout);
            for (int i = 0; i < emojiLayout1.getChildCount(); i++) {
                View child = emojiLayout1.getChildAt(i);
                if (child.getId() != emoji_id)
                    child.setVisibility(View.GONE);
            }

            LinearLayout emojiLayout2 = (LinearLayout) rootView.findViewById(R.id.emotions2_linear_layout);
            for (int i = 0; i < emojiLayout2.getChildCount(); i++) {
                View child = emojiLayout2.getChildAt(i);
                if (child.getId() != emoji_id)
                    child.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Fade out the view asking the user how they are feeling
     */
    private void animateOutEmotionPrompt() {
        View rootView = getView();
        if(rootView != null) {
            LinearLayout emotionsLinearLayout = (LinearLayout) rootView.findViewById(R.id.emotions_linear_layout);
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) emotionsLinearLayout.getLayoutParams();
            p.addRule(RelativeLayout.BELOW, R.id.main_header_text_view);
            emotionsLinearLayout.setLayoutParams(p);

            headerTextView.setText(R.string.recommendations_title);
        }
    }

    /**
     * Animate the recommendations into the screen, fading in and sliding up
     * from the bottom of the screen
     * @param layout The ViewGroup containing the recommendations
     */
    private void animateInRecommendations(final ViewGroup layout) {
        // Prepare the View for the animation
        layout.setVisibility(View.VISIBLE);
        layout.setAlpha(0.0f);

        // Make it invisible and move it to the bottom of the screen
        layout.animate()
                .translationY(800).setDuration(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                // Slide the view up and fade in
                layout.animate()
                        .translationY(0)
                        .alpha(1.0f).setDuration(500);
            }
        });
    }

}
