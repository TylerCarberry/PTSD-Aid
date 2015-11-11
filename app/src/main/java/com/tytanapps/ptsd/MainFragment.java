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
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        rootView.findViewById(R.id.happy_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emotionSelected(v);
            }
        });

        rootView.findViewById(R.id.ok_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emotionSelected(v);
            }
        });

        rootView.findViewById(R.id.sad_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emotionSelected(v);
            }
        });

        rootView.findViewById(R.id.button_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

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
            View signInButton = getView().findViewById(R.id.button_sign_in);
            if(signInButton != null)
                signInButton.setVisibility(View.INVISIBLE);
        }
    }

    private String getSharedPreferenceString(String prefKey, String defaultValue) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(prefKey, defaultValue);
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
     * Precondition: MainFragment is a member of MainActivity
     * @return Whether the user is signed in
     */
    private boolean isUserSignedIn() {
        return ((MainActivity) getActivity()).isUserSignedIn();
    }



    private void emotionSelected(View emotionPressed) {
        FrameLayout parentFrameLayout = (FrameLayout) getView().findViewById(R.id.recommendations_container);

        LinearLayout recommendationsLinearLayout = (LinearLayout) getView().findViewById(R.id.recommendations_linear_layout);
        recommendationsLinearLayout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        RelativeLayout emotionRecommendationLayout = (RelativeLayout) inflater.inflate(R.layout.recommendation_view, null, false);
        TextView emotionTextView = (TextView) emotionRecommendationLayout.findViewById(R.id.recommendation_textview);
        switch (emotionPressed.getId()) {
            case R.id.happy_face:
                emotionTextView.setText("Look at the resources to learn about possible symptoms of PTSD.");
                recommendationsLinearLayout.addView(emotionRecommendationLayout);
                emotionRecommendationLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDrawer();
                    }
                });
                break;
            case R.id.ok_face:
                emotionTextView.setText("Take the PTSD test to determine if you suffer from PTSD");
                recommendationsLinearLayout.addView(emotionRecommendationLayout);
                emotionTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDrawer();
                    }
                });
                break;
            case R.id.sad_face:
                // Don't recommend calling a trusted contact if one does not exist
                String trustedContactPhone = getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "");
                if(!trustedContactPhone.equals("")) {
                    emotionTextView.setText("Consider calling your trusted contact");
                    recommendationsLinearLayout.addView(emotionRecommendationLayout);
                    emotionRecommendationLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String phoneNumber = getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "");
                            if(!phoneNumber.equals(""))
                                ((MainActivity)getActivity()).openDialer(phoneNumber);
                            else
                                ((MainActivity)getActivity()).showTrustedContactDialog();
                        }
                    });
                }
                break;
        }

        if(!isUserSignedIn()) {
            RelativeLayout signInRecommendationLayout = (RelativeLayout) inflater.inflate(R.layout.recommendation_view, null, false);
            TextView signInTextView = (TextView) signInRecommendationLayout.findViewById(R.id.recommendation_textview);
            signInTextView.setText("Sign In");

            signInRecommendationLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signIn();
                }
            });

            recommendationsLinearLayout.addView(signInRecommendationLayout);
        }

        String trustedContactPhone = getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "");
        if(trustedContactPhone.equals("")) {
            RelativeLayout trustedContactRecommendationLayout = (RelativeLayout) inflater.inflate(R.layout.recommendation_view, null, false);
            TextView trustedContactTextView = (TextView) trustedContactRecommendationLayout.findViewById(R.id.recommendation_textview);
            trustedContactTextView.setText("Add a trusted contact");
            trustedContactRecommendationLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity)getActivity()).showTrustedContactDialog();
                }
            });

            recommendationsLinearLayout.addView(trustedContactRecommendationLayout);

        }

        animateInRecommendations(parentFrameLayout);
    }

    private void animateInRecommendations(final ViewGroup layout) {
        // Prepare the View for the animation
        layout.setVisibility(View.VISIBLE);
        layout.setAlpha(0.0f);

        // Start the animation
        layout.animate()
                .translationY(700).setDuration(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                layout.animate()
                        .translationY(0)
                        .alpha(1.0f).setDuration(1000);
            }
        });

    }

    // TODO: Rename method, update argument and hook method into UI event
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
