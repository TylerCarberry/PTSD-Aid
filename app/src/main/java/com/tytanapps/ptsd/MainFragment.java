package com.tytanapps.ptsd;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MainFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private static final String LOG_TAG = MainFragment.class.getSimpleName();

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        return rootView;
    }

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
        Log.d(LOG_TAG, "signIn() called with: " + "");
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

            RelativeLayout emotionRecommendationLayout = (RelativeLayout) inflater.inflate(R.layout.recommendation_view, null, false);
            TextView emotionTextView = (TextView) emotionRecommendationLayout.findViewById(R.id.recommendation_textview);

            emotionPressed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            switch (emotionPressed.getId()) {

                case R.id.happy_face:
                    emotionTextView.setText(R.string.recommendation_resources);
                    recommendationsLinearLayout.addView(emotionRecommendationLayout);
                    emotionRecommendationLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openDrawer();
                        }
                    });
                    fragmentView.findViewById(R.id.sad_face).setVisibility(View.GONE);
                    fragmentView.findViewById(R.id.ok_face).setVisibility(View.GONE);

                    animateOutEmotionPrompt();

                    break;
                case R.id.ok_face:
                    emotionTextView.setText(R.string.recommendation_test);
                    recommendationsLinearLayout.addView(emotionRecommendationLayout);
                    emotionTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openDrawer();
                        }
                    });
                    fragmentView.findViewById(R.id.sad_face).setVisibility(View.GONE);
                    fragmentView.findViewById(R.id.happy_face).setVisibility(View.GONE);

                    animateOutEmotionPrompt();
                    break;
                case R.id.sad_face:
                    // Don't recommend calling a trusted contact if one does not exist
                    String trustedContactPhone = getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "");
                    if (!trustedContactPhone.equals("")) {
                        emotionTextView.setText(R.string.recommendation_call_trusted_contact);
                        recommendationsLinearLayout.addView(emotionRecommendationLayout);
                        emotionRecommendationLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String phoneNumber = getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "");
                                if (!phoneNumber.equals(""))
                                    ((MainActivity) getActivity()).openDialer(phoneNumber);
                                else
                                    ((MainActivity) getActivity()).showCreateTrustedContactDialog();
                            }
                        });
                    }
                    fragmentView.findViewById(R.id.happy_face).setVisibility(View.GONE);
                    fragmentView.findViewById(R.id.ok_face).setVisibility(View.GONE);

                    animateOutEmotionPrompt();
                    break;
            }

            if (!isUserSignedIn()) {
                RelativeLayout signInRecommendationLayout = (RelativeLayout) inflater.inflate(R.layout.recommendation_view, null, false);
                TextView signInTextView = (TextView) signInRecommendationLayout.findViewById(R.id.recommendation_textview);
                signInTextView.setText(R.string.recommendation_sign_in);

                signInRecommendationLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signIn();
                    }
                });

                recommendationsLinearLayout.addView(signInRecommendationLayout);
            }

            String trustedContactPhone = getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "");
            if (trustedContactPhone.equals("")) {
                RelativeLayout trustedContactRecommendationLayout = (RelativeLayout) inflater.inflate(R.layout.recommendation_view, null, false);
                TextView trustedContactTextView = (TextView) trustedContactRecommendationLayout.findViewById(R.id.recommendation_textview);
                trustedContactTextView.setText(R.string.recommendation_add_trusted_contact);
                trustedContactRecommendationLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) getActivity()).showCreateTrustedContactDialog();
                    }
                });

                recommendationsLinearLayout.addView(trustedContactRecommendationLayout);
            }

            animateInRecommendations(parentFrameLayout);
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

            View promptEmotionTextView = fragmentView.findViewById(R.id.prompt_emotion);
            fadeOutAndRemoveView(promptEmotionTextView, 200);

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
                        .alpha(1.0f).setDuration(1000);
            }
        });
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
