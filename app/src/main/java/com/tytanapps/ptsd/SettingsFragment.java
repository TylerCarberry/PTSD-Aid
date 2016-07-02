package com.tytanapps.ptsd;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;

import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsFragment extends PreferenceFragment {

    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onStart() {
        super.onStart();

        setupNewsNotificationPref();
        setupEnableTrustedContactPref();
        setupChangeTrustedContactPref();
        setupFeedbackButton();

    }

    private void setupNewsNotificationPref() {
        CheckBoxPreference newsPreference = (CheckBoxPreference) findPreference(getString(R.string.pref_news_notification));
        newsPreference.setChecked(getSharedPreferenceBoolean(getString(R.string.pref_news_notification), true));
        newsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if((Boolean) newValue) {
                    FirebaseMessaging.getInstance().subscribeToTopic("news");
                    Log.d(LOG_TAG, "onCheckedChanged: subscribed");
                }
                else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
                    Log.d(LOG_TAG, "onCheckedChanged: unsubscribed");
                }

                return true;
            }
        });
    }

    private void setupChangeTrustedContactPref() {
        Preference changeTrustedContactPreference = findPreference("change_trusted_contact");

        final String contactName = getSharedPreferenceString(getString(R.string.pref_trusted_name_key), "");
        if(contactName.equals("")) {
            changeTrustedContactPreference.setTitle("Add trusted contact");
            changeTrustedContactPreference.setSummary("");
        } else {
            changeTrustedContactPreference.setTitle("Change trusted contact");
            changeTrustedContactPreference.setSummary("Your trusted contact is " + contactName);
        }

        changeTrustedContactPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity)getActivity()).pickTrustedContact();
                return true;
            }
        });
    }

    private void setupEnableTrustedContactPref() {
        Preference enableTrustedContactPreference = findPreference("enable_trusted_contact");

        enableTrustedContactPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getActivity().findViewById(R.id.fab).setVisibility(((Boolean)newValue) ? View.VISIBLE : View.INVISIBLE);
                saveSharedPreference("enable_trusted_contact", (Boolean) newValue);

                return true;
            }
        });
    }

    private void setupFeedbackButton() {
        Preference provideFeedback = findPreference("provide_feedback");
        provideFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity)getActivity()).provideFeedback();

                return true;
            }
        });
    }

    /**
     * Read a shared preference string from memory
     * @param prefKey The key of the shared preference
     * @param defaultValue The value to return if the key does not exist
     * @return The shared preference with the given key
     */
    private String getSharedPreferenceString(String prefKey, String defaultValue) {
        return getActivity().getPreferences(Context.MODE_PRIVATE).getString(prefKey, defaultValue);
    }

    /**
     * Read a shared preference string from memory
     * @param prefKey The key of the shared preference
     * @param defaultValue The value to return if the key does not exist
     * @return The shared preference with the given key
     */
    private boolean getSharedPreferenceBoolean(String prefKey, boolean defaultValue) {
        return getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(prefKey, defaultValue);
    }

    /**
     * Save a String to a SharedPreference
     * @param prefKey The key of the shared preference
     * @param value The value to save in the shared preference
     */
    private void saveSharedPreference(String prefKey, String value) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(prefKey, value);
        editor.apply();
    }

    /**
     * Save a String to a SharedPreference
     * @param prefKey The key of the shared preference
     * @param value The value to save in the shared preference
     */
    private void saveSharedPreference(String prefKey, boolean value) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(prefKey, value);
        editor.apply();
    }


}