package com.tytanapps.ptsd;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
                emotionHappy();
            }
        });

        rootView.findViewById(R.id.meh_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emotionOk();
            }
        });

        rootView.findViewById(R.id.sad_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emotionSad();
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

    @Override
    public void onStart() {
        super.onStart();

        if(isUserSignedIn()) {
            getView().findViewById(R.id.button_sign_in).setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Sign in to the user's Google Account
     */
    private void signIn() {
        Activity parentActivity = getActivity();
        if(parentActivity instanceof MainActivity)
            ((MainActivity) getActivity()).signIn();
    }

    private boolean isUserSignedIn() {
        return ((MainActivity) getActivity()).isUserSignedIn();
    }

    private void emotionOk() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.emotion_result, null, false);

        TextView recommendations = (TextView) layout.findViewById(R.id.recommendation_textview);
        recommendations.setText("Take the stress test to determine if you suffer from PTSD");

        animateInRecommendations(layout);
    }

    private void emotionHappy() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.emotion_result, null, false);

        TextView recommendations = (TextView) layout.findViewById(R.id.recommendation_textview);
        recommendations.setText("Great! Look at the resources for veterans.");

        animateInRecommendations(layout);
    }

    private void emotionSad() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.emotion_result, null, false);

        TextView recommendations = (TextView) layout.findViewById(R.id.recommendation_textview);
        recommendations.setText("Consider talking with someone");

        animateInRecommendations(layout);
    }

    private void animateInRecommendations(final ViewGroup layout) {
        FrameLayout recommendationContainer = (FrameLayout) getView().findViewById(R.id.inner_frame);
        recommendationContainer.removeAllViews();

        // Prepare the View for the animation
        layout.setVisibility(View.VISIBLE);
        layout.setAlpha(0.0f);

        recommendationContainer.addView(layout);


        // Start the animation
        layout.animate()
                .translationY(500).setDuration(0).setListener(new AnimatorListenerAdapter() {
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
