package com.tytanapps.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PhoneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhoneFragment extends Fragment {
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
     * @return A new instance of fragment PhoneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhoneFragment newInstance(String param1, String param2) {
        PhoneFragment fragment = new PhoneFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public PhoneFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_phone, container, false);


        CardView veteransCrisisLineCardView = (CardView) rootView.findViewById(R.id.veterans_crisis_line_cardview);
        veteransCrisisLineCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialer(getString(R.string.phone_veterans_crisis_line));
            }
        });

        CardView suicideLifelineCardView = (CardView) rootView.findViewById(R.id.suicide_lifeline_cardview);
        suicideLifelineCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialer(getString(R.string.phone_suicide_lifeline));
            }
        });

        CardView alcoholismCardView = (CardView) rootView.findViewById(R.id.alcoholism_cardview);
        alcoholismCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialer(getString(R.string.phone_alcoholism));
            }
        });

        CardView veteransFoundationCardView = (CardView) rootView.findViewById(R.id.veterans_foundation_cardview);
        veteransFoundationCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialer(getString(R.string.phone_veterans_foundation_hotline));
            }
        });

        return rootView;
    }

    /**
     * Open the dialer with a phone number entered
     * This does not call the number directly, the user needs to press the call button
     * @param phoneNumber The phone number to call
     */
    private void openDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
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
