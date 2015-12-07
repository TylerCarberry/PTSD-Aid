package com.tytanapps.ptsd;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Displays a list of websites to find more information about PTSD. Shows a brief description for
 * each website. Tapping on the card opens the website.
 */
public class WebsiteFragment extends Fragment {

    public WebsiteFragment() {
        // Required empty public constructor
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

}
