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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
        //Log.d(LOG_TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

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
        return rootView;
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
     * Open the navigation drawer
     */
    private void openDrawer() {
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.openDrawer(Gravity.LEFT);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Hide the sign in button if the user is already signed in
        if(isUserSignedIn()) {
            View fragmentView = getView();
            if(fragmentView != null) {
                View signInButton = fragmentView.findViewById(R.id.button_sign_in);
                if (signInButton != null)
                    signInButton.setVisibility(View.INVISIBLE);
            }
        }
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

    /**
     * Sign in to the user's Google Account
     */
    private void signIn() {
        //Log.d(LOG_TAG, "signIn() called with: " + "");
        Activity parentActivity = getActivity();
        if(parentActivity instanceof MainActivity)
            ((MainActivity) getActivity()).signIn();
    }

    /**
     * Determine whether the user is signed in to their Google account
     * Precondition: MainFragment is a member of MainActivity
     * @return Whether the user is signed in
     */
    private boolean isUserSignedIn() {
        return ((MainActivity) getActivity()).isUserSignedIn();
    }


    private void emotionSelected(View emotionPressed) {
        View fragmentView = getView();
        if(fragmentView != null) {
            FrameLayout parentFrameLayout = (FrameLayout) fragmentView.findViewById(R.id.recommendations_container);

            LinearLayout recommendationsLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.recommendations_linear_layout);
            recommendationsLinearLayout.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(getActivity());

            RelativeLayout emotionRecommendationLayout = (RelativeLayout) inflater.inflate(R.layout.recommendation_view, getViewGroup(), false);
            TextView emotionTextView = (TextView) emotionRecommendationLayout.findViewById(R.id.recommendation_textview);

            // Remove on click listener from the emoji
            emotionPressed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            fadeOutAllEmojiExcept(emotionPressed.getId());
            animateOutEmotionPrompt();

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
                    if (isUserSignedIn())
                        recommendationsLinearLayout.addView(getSuggestionCallTrustedContact());

                    recommendationsLinearLayout.addView(getSuggestionJoinVeteranAssociation());

                    break;

                case R.id.sick_face:
                    recommendationsLinearLayout.addView(getSuggestionVisitResources());

                    break;

                case R.id.poop_emoji:
                    if (trustedContactCreated())
                        recommendationsLinearLayout.addView(getSuggestionCallTrustedContact());

                    recommendationsLinearLayout.addView(getSuggestionCallSuicideLifeline());

                    break;
            }

            //if (!isUserSignedIn())
            //    recommendationsLinearLayout.addView(getSuggestionSignIn());

            if (!trustedContactCreated())
                recommendationsLinearLayout.addView(getSuggestionAddTrustedContact());

            animateInRecommendations(parentFrameLayout);
        }
    }

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
     * When tapped, do nothing TODO
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionVAWebsite() {
        return createSuggestionLayout(getString(R.string.recommendation_veteran_benefits), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //signIn();
            }
        });
    }
    /**
     * Get the recommendation to join a veteran accociation
     * When tapped, do nothing TODO
     * @return The Relative Layout containing the suggestion
     */
    private RelativeLayout getSuggestionJoinVeteranAssociation() {
        return createSuggestionLayout(getString(R.string.recommendation_veteran_association), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //signIn();
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
                    openDialer(phoneNumber);
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
    private RelativeLayout getSuggestionCallSuicideLifeline() {
        return createSuggestionLayout(getString(R.string.recommendation_call_suicide_lifeline), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = getSharedPreferenceString(getString(R.string.phone_suicide_lifeline), "");
                openDialer(phoneNumber);
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
                openDrawer();
            }
        });

    }

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

    private void fadeOutAllEmojiExcept(int emoji_id) {
        LinearLayout emojiLayout1 = (LinearLayout) getView().findViewById(R.id.emotions_linear_layout);
        for(int i = 0; i < emojiLayout1.getChildCount(); i++) {
            View child = emojiLayout1.getChildAt(i);
            if(child.getId() != emoji_id)
                child.setVisibility(View.GONE);
        }

        LinearLayout emojiLayout2 = (LinearLayout) getView().findViewById(R.id.emotions2_linear_layout);
        for(int i = 0; i < emojiLayout2.getChildCount(); i++) {
            View child = emojiLayout2.getChildAt(i);
            if(child.getId() != emoji_id)
                child.setVisibility(View.GONE);
        }
    }

    /**
     * Fade out the view asking the user how they are feeling
     */
    private void animateOutEmotionPrompt() {
        View fragmentView = getView();
        if(fragmentView != null) {

            LinearLayout emotionsLinearLayout = (LinearLayout) fragmentView.findViewById(R.id.emotions_linear_layout);
            RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) emotionsLinearLayout.getLayoutParams();
            p.addRule(RelativeLayout.BELOW, R.id.main_header_text_view);
            emotionsLinearLayout.setLayoutParams(p);

            //View promptEmotionTextView = fragmentView.findViewById(R.id.prompt_emotion);
            //fadeOutAndRemoveView(promptEmotionTextView, 200);

            TextView headerTextView = (TextView) fragmentView.findViewById(R.id.main_header_text_view);
            headerTextView.setText(R.string.recommendations_title);
        }
    }

    /**
     * Fade a view out and remove it once it has fully vanished
     * @param view The view to fade out and remove
     * @param duration The duration of the fade out in milliseconds
     */
    private void fadeOutAndRemoveView(final View view, int duration) {
        // Start the animation
        view.animate().alpha(0.0f)
                .setDuration(duration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });
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
    private void openDialer(String phoneNumber) {

        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(getActivity(), R.string.error_open_dialer, Toast.LENGTH_SHORT).show();
        }
    }

}
