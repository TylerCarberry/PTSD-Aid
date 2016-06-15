package com.tytanapps.ptsd.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.tytanapps.ptsd.MainActivity;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.RemoteConfigurable;


/**
 * The main fragment displayed when you launch the app. Prompts the user for their emotion
 * and gives them recommendations based on their answer.
 */
public class MainFragment extends Fragment {

    private static final String LOG_TAG = MainFragment.class.getSimpleName();

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setupEmotions(rootView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Hide the sign in button if the user is already signed in
        if(isUserSignedIn()) {
            View rootView = getView();
            if(rootView != null) {
                View signInButton = rootView.findViewById(R.id.button_sign_in);
                if (signInButton != null)
                    signInButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Get the root view of the fragment casted to a ViewGroup
     * @return The root view of the fragment as a ViewGroup
     */
    private ViewGroup getViewGroup() {
        View rootView = getView();
        if(rootView instanceof ViewGroup)
            return (ViewGroup) getView();
        return null;
    }

    /**
     * Add listeners to the emoji buttons to show the suggestions when tapped
     * Hides the extra emoji if show_extra_emoji is false on remote config
     * @param rootView The root view of the fragment, containing the emotion buttons
     */
    private void setupEmotions(View rootView) {
        View.OnClickListener emotionSelectedListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emotionSelected(v);
            }
        };

        rootView.findViewById(R.id.happy_face).setOnClickListener(emotionSelectedListener);
        rootView.findViewById(R.id.ok_face).setOnClickListener(emotionSelectedListener);
        rootView.findViewById(R.id.sad_face).setOnClickListener(emotionSelectedListener);

        if(getActivity() instanceof RemoteConfigurable) {
            FirebaseRemoteConfig remoteConfig = ((RemoteConfigurable)getActivity()).getRemoteConfig();

            if(remoteConfig.getBoolean("show_extra_emoji")){
                rootView.findViewById(R.id.sick_face).setOnClickListener(emotionSelectedListener);
                rootView.findViewById(R.id.poop_emoji).setOnClickListener(emotionSelectedListener);
            }
            else {
                rootView.findViewById(R.id.emotions2_linear_layout).setVisibility(View.GONE);
            }
        }
        else {
            rootView.findViewById(R.id.sick_face).setOnClickListener(emotionSelectedListener);
            rootView.findViewById(R.id.poop_emoji).setOnClickListener(emotionSelectedListener);
        }
    }

    /**
     * Open the navigation drawer
     */
    private void openDrawer() {
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.openDrawer(Gravity.LEFT);
    }

    /**
     * Sign in to the user's Google Account
     */
    private void signIn() {
        Activity parentActivity = getActivity();
        if(parentActivity instanceof MainActivity)
            ((MainActivity) getActivity()).signIn();
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
     * When an emotion icon is tapped, show the corresponding recommendations and hide the other icons
     * @param emotionPressed The emotion icon that was tapped
     */
    private void emotionSelected(View emotionPressed) {
        View fragmentView = getView();
        if(fragmentView != null) {
            ViewGroup parentFrameLayout = (ViewGroup) fragmentView.findViewById(R.id.recommendations_container);

            LinearLayout recommendationsLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.recommendations_linear_layout);
            recommendationsLinearLayout.removeAllViews();

            // Remove on click listener from the emoji
            emotionPressed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {}
            });

            fadeOutAllEmojiExcept(emotionPressed.getId());
            animateOutEmotionPrompt();

            // Show the suggestions for each emotion
            switch (emotionPressed.getId()) {
                case R.id.happy_face:
                    recommendationsLinearLayout.addView(getSuggestionVAWebsite());
                    recommendationsLinearLayout.addView(getSuggestionVisitResources());
                    break;

                case R.id.ok_face:
                    recommendationsLinearLayout.addView(getSuggestionVisitResources());
                    recommendationsLinearLayout.addView(getSuggestionJoinVeteranAssociation());
                    break;

                case R.id.sad_face:
                    if (trustedContactCreated())
                        recommendationsLinearLayout.addView(getSuggestionCallTrustedContact());
                    recommendationsLinearLayout.addView(getSuggestionJoinVeteranAssociation());
                    recommendationsLinearLayout.addView(getSuggestionCallVeteranFoundation());
                    break;

                case R.id.sick_face:
                    recommendationsLinearLayout.addView(getSuggestionFindFacility());
                    if (trustedContactCreated())
                        recommendationsLinearLayout.addView(getSuggestionCallTrustedContact());
                    recommendationsLinearLayout.addView(getSuggestionVisitResources());
                    break;

                case R.id.poop_emoji:
                    if (trustedContactCreated())
                        recommendationsLinearLayout.addView(getSuggestionCallTrustedContact());
                    recommendationsLinearLayout.addView(getSuggestionCallVeteransCrisisLine());
                    recommendationsLinearLayout.addView(getSuggestionJoinVeteranAssociation());
                    break;
            }

            if (!trustedContactCreated())
                recommendationsLinearLayout.addView(getSuggestionAddTrustedContact());

            animateInRecommendations(parentFrameLayout);
        }
    }

    /**
     * Get a blank suggestion layout
     * @return A blank suggestion layout
     */
    private RelativeLayout getSuggestionLayoutTemplate() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        return (RelativeLayout) inflater.inflate(R.layout.recommendation_view, getViewGroup(), false);
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
                signIn();
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
                openBrowserIntent(getString(R.string.website_va));
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
               openBrowserIntent(getString(R.string.veterans_network_website));
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
                    openDialerIntent(phoneNumber);
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
                openDialerIntent(phoneNumber);
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
                openDialerIntent(phoneNumber);
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
                    ((MainActivity)getActivity()).switchFragment(new NearbyFacilitiesFragment());
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
    private boolean trustedContactCreated() {
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

            TextView headerTextView = (TextView) rootView.findViewById(R.id.main_header_text_view);
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

    /**
     * Open the dialer with a phone number entered
     * This does not call the number directly, the user needs to press the call button
     * @param phoneNumber The phone number to call
     */
    private void openDialerIntent(String phoneNumber) {

        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(getActivity(), R.string.error_open_dialer, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Open a website in the browser
     * Precondition: url is a valid url
     * @param url The url to open
     */
    private void openBrowserIntent(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    /**
     * Get a shared preference String from a saved file
     * @param prefKey The key of the String
     * @param defaultValue The default value if no key exists
     * @return The shared preference String with the given key
     */
    private String getSharedPreferenceString(String prefKey, String defaultValue) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(prefKey, defaultValue);
    }

}
