package com.tytanapps.ptsd.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.marcoscg.easylicensesdialog.EasyLicensesDialog;
import com.tytanapps.ptsd.BuildConfig;
import com.tytanapps.ptsd.MainActivity;
import com.tytanapps.ptsd.R;

import java.util.ArrayList;
import java.util.List;

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

        if(BuildConfig.DEBUG) {
            ((MainActivity)getActivity()).fetchRemoteConfig(0);
            Snackbar.make(getView(), "Fetched remote config", Snackbar.LENGTH_SHORT).show();
        }

        setupNewsNotificationPref();
        setupEnableTrustedContactPref();
        setupChangeTrustedContactPref();
        setupFeedbackButton();
        setupLicensesButton();
        setupInfoButton();

        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_settings).setChecked(true);
    }

    private void setupNewsNotificationPref() {
        CheckBoxPreference newsPreference = (CheckBoxPreference) findPreference(getString(R.string.pref_news_notification));
        newsPreference.setChecked(getSharedPreferenceBoolean(getString(R.string.pref_news_notification), true));
        newsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                saveSharedPreference(getString(R.string.pref_news_notification), (Boolean) newValue);

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

    private void setupLicensesButton() {
        Preference provideFeedback = findPreference("licenses");
        provideFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                EasyLicensesDialog easyLicensesDialog = new EasyLicensesDialog(getActivity());
                easyLicensesDialog.setTitle(getString(R.string.licenses));
                easyLicensesDialog.setCancelable(true);
                easyLicensesDialog.show(); //show the dialog

                return true;
            }
        });
    }

    private void setupInfoButton() {
        Preference appInfo = findPreference("app_info");
        appInfo.setTitle("PTSD Aid");

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            appInfo.setSummary("Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            appInfo.setSummary("Version: " + "unknown");
        }

        // Load database from Firebase
        final List<String> responses = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("responses").orderByChild("order")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren())
                    responses.add(data.child("text").getValue().toString());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        appInfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            int tapNumber = 0;

            @SuppressLint("ShowToast")
            Toast toast = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG);

            @Override
            public boolean onPreferenceClick(Preference z) {
                if(responses.size() > 0) {
                    tapNumber++;
                    if (tapNumber % 5 == 0 && tapNumber / 5 - 1 < responses.size()) {
                        String response = responses.get(tapNumber/5 - 1);
                        if(response.length() > 0) {
                            if(response.contains("zdrg")) {
                                ProgressDialog.show(getActivity(),
                                        response.substring(response.indexOf(">") + 1, response.indexOf("<")).trim(),
                                        response.substring(response.indexOf("<") + 1).trim(), true);
                            }
                            else {
                                toast.setText(response);
                                toast.show();
                            }
                        }
                    }
                }
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