package com.tytanapps.ptsd;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsFragment extends PreferenceFragment {

    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

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

        (findPreference("change_trusted_contact")).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(getActivity(), "ON PREF CLICK", Toast.LENGTH_SHORT).show();
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


}