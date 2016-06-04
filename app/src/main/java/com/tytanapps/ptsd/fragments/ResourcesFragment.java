package com.tytanapps.ptsd.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.RemoteConfigurable;


/**
 * Shows information about PTSD. Gives symptoms, causes, and treatment for PTSD.
 */
public class ResourcesFragment extends Fragment {

    public ResourcesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_resources, container, false);

        if(getActivity() instanceof RemoteConfigurable) {
            FirebaseRemoteConfig remoteConfig = ((RemoteConfigurable)getActivity()).getRemoteConfig();

            updateRemoteText(remoteConfig, (TextView) rootView.findViewById(R.id.symptoms_textview), "symptoms_text");
            updateRemoteText(remoteConfig, (TextView) rootView.findViewById(R.id.causes_textview), "causes_text");
            updateRemoteText(remoteConfig, (TextView) rootView.findViewById(R.id.diagnosis_textview), "diagnosis_text");
            updateRemoteText(remoteConfig, (TextView) rootView.findViewById(R.id.treatment_textview), "treatment_text");
            updateRemoteText(remoteConfig, (TextView) rootView.findViewById(R.id.counseling_textview), "counseling_text");
        }

        return rootView;
    }

    private void updateRemoteText(FirebaseRemoteConfig remoteConfig, TextView textView, String key) {
        String text = remoteConfig.getString(key);
        text = text.replaceAll("%", "\n");
        textView.setText(text);
    }

}
