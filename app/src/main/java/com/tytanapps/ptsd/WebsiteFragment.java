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
 * Use the {@link WebsiteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WebsiteFragment extends Fragment {
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
     * @return A new instance of fragment WebsiteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WebsiteFragment newInstance(String param1, String param2) {
        WebsiteFragment fragment = new WebsiteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public WebsiteFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_website, container, false);


        CardView veteransChatCard = (CardView) rootView.findViewById(R.id.veterans_chat_cardview);
        veteransChatCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://www.veteranscrisisline.net/ChatTermsOfService.aspx?account=Veterans%20Chat");
            }
        });

        CardView veteransQuizCard = (CardView) rootView.findViewById(R.id.veterans_quiz_cardview);
        veteransQuizCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("https://www.vetselfcheck.org/Welcome.cfm");
            }
        });

        CardView nimhCard = (CardView) rootView.findViewById(R.id.nimh_cardview);
        nimhCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("http://www.nimh.nih.gov/health/topics/post-traumatic-stress-disorder-ptsd/index.shtml");
            }
        });

        CardView vaCard = (CardView) rootView.findViewById(R.id.va_cardview);
        vaCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("http://www.ptsd.va.gov/public/index.asp");
            }
        });

        CardView ptsdCoachCard = (CardView) rootView.findViewById(R.id.ptsd_coach_cardview);
        ptsdCoachCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser("http://www.ptsd.va.gov/apps/ptsdcoachonline/default.htm");
            }
        });



        return rootView;
    }

    /**
     * Open a website in the browser
     * Precondition: url is a valid url
     * @param url The url to open
     */
    private void openBrowser(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
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
