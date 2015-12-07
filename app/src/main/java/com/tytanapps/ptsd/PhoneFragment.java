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
 * Displays a list of common veteran hotlines. Shows a brief description for each hotline and
 * the phone number to call. Tapping on the hotline calls them.
 */
public class PhoneFragment extends Fragment {

    public PhoneFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

}
