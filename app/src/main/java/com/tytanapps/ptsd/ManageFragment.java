package com.tytanapps.ptsd;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ManageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String LOG_TAG = ManageFragment.class.getName();

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
     * @return A new instance of fragment ManageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ManageFragment newInstance(String param1, String param2) {
        ManageFragment fragment = new ManageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ManageFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_manage, container, false);

        EditText ageEditText = (EditText) rootView.findViewById(R.id.age_edittext);
        ageEditText.setText(""+getSharedPreferenceInt(getString(R.string.pref_age_key), 18));
        ageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int age = Integer.parseInt(s.toString());
                    saveSharedPreference(getString(R.string.pref_age_key), age);
                } catch (NumberFormatException e) {
                    // The user has not entered a valid age. Do not save the input.
                    // This also occurs when the EditText is empty ""
                }
            }
        });

        int gender = getSharedPreferenceInt(getString(R.string.pref_gender_key), -1);
        switch (gender) {
            case R.id.male_radiobutton:
                RadioButton maleRadioButton = (RadioButton) rootView.findViewById(R.id.male_radiobutton);
                maleRadioButton.toggle();
                break;
            case R.id.female_radiobutton:
                RadioButton femaleRadioButton = (RadioButton) rootView.findViewById(R.id.female_radiobutton);
                femaleRadioButton.toggle();
                break;
            case R.id.other_gender_radiobutton:
                RadioButton otherGenderRadioButton = (RadioButton) rootView.findViewById(R.id.other_gender_radiobutton);
                otherGenderRadioButton.toggle();
                break;
        }

        RadioGroup genderRadioGroup = (RadioGroup) rootView.findViewById(R.id.gender_radiogroup);
        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                saveSharedPreference(getString(R.string.pref_gender_key), checkedId);
            }
        });

        CheckBox armyCheckBox = (CheckBox) rootView.findViewById(R.id.army_checkbox);
        boolean army = getSharedPreferenceBoolean(getString(R.string.pref_army_key), false);
        if(army)
            armyCheckBox.toggle();
        armyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSharedPreference(getString(R.string.pref_army_key), isChecked);
            }
        });
        CheckBox navyCheckBox = (CheckBox) rootView.findViewById(R.id.navy_checkbox);
        boolean navy = getSharedPreferenceBoolean(getString(R.string.pref_navy_key), false);
        if(navy)
            navyCheckBox.toggle();
        navyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSharedPreference(getString(R.string.pref_navy_key), isChecked);
            }
        });

        CheckBox airCheckBox = (CheckBox) rootView.findViewById(R.id.air_checkbox);
        boolean air = getSharedPreferenceBoolean(getString(R.string.pref_air_key), false);
        if(air)
            airCheckBox.toggle();
        airCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSharedPreference(getString(R.string.pref_air_key), isChecked);
            }
        });

        CheckBox marinesCheckBox = (CheckBox) rootView.findViewById(R.id.marine_checkbox);
        boolean marines = getSharedPreferenceBoolean(getString(R.string.pref_marines_key), false);
        if(marines)
            marinesCheckBox.toggle();
        marinesCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveSharedPreference(getString(R.string.pref_marines_key), isChecked);
            }
        });

        TextView trustedNameTextview = (TextView) rootView.findViewById(R.id.trusted_contact_name_textview);
        trustedNameTextview.setText(getSharedPreferenceString(getString(R.string.pref_trusted_name_key), "None"));

        TextView trustedPhoneTextview = (TextView) rootView.findViewById(R.id.trusted_contact_phone_textview);
        trustedPhoneTextview.setText(getSharedPreferenceString(getString(R.string.pref_trusted_phone_key), "None"));

        Button debugButton = (Button) rootView.findViewById(R.id.change_contact_button);
        debugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickTrustedContact();
            }
        });

        return rootView;
    }

    private void pickTrustedContact() {
        Activity parentActivity = getActivity();
        if(parentActivity instanceof MainActivity)
            ((MainActivity)getActivity()).pickTrustedContact();
    }

    private void saveSharedPreference(String prefKey, String value) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(prefKey, value);
        editor.apply();
    }

    private void saveSharedPreference(String prefKey, int value) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(prefKey, value);
        editor.apply();
    }

    private void saveSharedPreference(String prefKey, boolean value) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(prefKey, value);
        editor.apply();
    }

    private String getSharedPreferenceString(String prefKey, String defaultValue) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(prefKey, defaultValue);
    }

    private int getSharedPreferenceInt(String prefKey, int defaultValue) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(prefKey, defaultValue);
    }

    private boolean getSharedPreferenceBoolean(String prefKey, boolean defaultValue) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(prefKey, defaultValue);
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
