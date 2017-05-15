package com.tytanapps.ptsd.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.tytanapps.ptsd.BuildConfig;
import com.tytanapps.ptsd.MainActivity;
import com.tytanapps.ptsd.PTSDApplication;
import com.tytanapps.ptsd.R;
import com.tytanapps.ptsd.firebase.RemoteConfig;
import com.tytanapps.ptsd.utils.ExternalAppUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static butterknife.ButterKnife.findById;

public class SettingsFragment extends PreferenceFragment {

    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    @Inject RemoteConfig remoteConfig;
    @Inject FirebaseMessaging firebaseMessaging;
    @Inject FirebaseDatabase database;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ((PTSDApplication)getActivity().getApplication()).getFirebaseComponent().inject(this);
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (BuildConfig.DEBUG && getView() != null) {
            remoteConfig.fetch(0);
            Snackbar.make(getView(), "Fetched remote config", Snackbar.LENGTH_SHORT).show();
        }

        setupSettings();

        NavigationView navigationView = findById(getActivity(), R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_settings).setChecked(true);

        getActivity().setTitle(R.string.settings_title);
    }

    private void setupSettings() {
        setupIsVeteranPref();
        setupNewsNotificationPref();
        setupEnableTrustedContactPref();
        setupChangeTrustedContactPref();
        setupFeedbackButton();
        setupLicensesButton();
        setupInfoButton();
        setupGithubButton();
        setupPrivacyPolicyButton();
    }

    private void setupNewsNotificationPref() {
        CheckBoxPreference newsPreference = (CheckBoxPreference) findPreference(getString(R.string.pref_news_notification));
        newsPreference.setChecked(getSharedPreferenceBoolean(getString(R.string.pref_news_notification), true));
        newsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                saveSharedPreference(getString(R.string.pref_news_notification), (Boolean) newValue);

                if ((Boolean) newValue) {
                    firebaseMessaging.subscribeToTopic("news");
                } else {
                    firebaseMessaging.unsubscribeFromTopic("news");
                }

                return true;
            }
        });
    }

    private void setupIsVeteranPref() {
        CheckBoxPreference newsPreference = (CheckBoxPreference) findPreference(getString(R.string.pref_veteran));
        newsPreference.setChecked(getSharedPreferenceBoolean(getString(R.string.pref_veteran), true));
        newsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                saveSharedPreference(getString(R.string.pref_veteran), (Boolean) newValue);
                return true;
            }
        });
    }


    private void setupChangeTrustedContactPref() {
        Preference changeTrustedContactPreference = findPreference(getString(R.string.pref_change_trusted_contact));

        final String contactName = getSharedPreferenceString(getString(R.string.pref_trusted_name_key), "");
        if (contactName.equals("")) {
            changeTrustedContactPreference.setTitle(getString(R.string.add_trusted_contact));
            changeTrustedContactPreference.setSummary("");
        } else {
            changeTrustedContactPreference.setTitle(getString(R.string.change_trusted_contact));
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
        Preference enableTrustedContactPreference = findPreference(getString(R.string.pref_enable_trusted_contact));

        enableTrustedContactPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                findById(getActivity(), R.id.fab).setVisibility(((Boolean)newValue) ? View.VISIBLE : View.INVISIBLE);
                saveSharedPreference(getString(R.string.pref_enable_trusted_contact), (Boolean) newValue);

                return true;
            }
        });
    }

    private void setupGithubButton() {
        Preference pref = findPreference(getString(R.string.pref_github));
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ExternalAppUtil.openBrowserIntent(SettingsFragment.this, getString(R.string.github_url));

                return true;
            }
        });
    }


    private void setupFeedbackButton() {
        Preference provideFeedback = findPreference(getString(R.string.pref_feedback));
        provideFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ((MainActivity)getActivity()).provideFeedback();

                return true;
            }
        });
    }

    private void setupPrivacyPolicyButton() {
        Preference provideFeedback = findPreference(getString(R.string.pref_privacy_policy));
        provideFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ExternalAppUtil.openBrowserIntent(SettingsFragment.this, getString(R.string.privacy_policy_url));

                return true;
            }
        });
    }
    
    private void setupLicensesButton() {
        Preference preference = findPreference(getString(R.string.pref_licenses));
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                new LibsBuilder()
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .withActivityTitle(getString(R.string.open_source_libraries))
                        .withAutoDetect(true)
                        .start(getActivity());

                return true;
            }
        });
    }

    private void setupInfoButton() {
        Preference appInfo = findPreference(getString(R.string.pref_app_info));
        appInfo.setTitle(getString(R.string.app_name));

        String version = ExternalAppUtil.getApkVersionName(getActivity());
        appInfo.setSummary("Version: " + version);

        // Load database from Firebase
        final List<String> responses = new ArrayList<>();
        database.getReference("responses").orderByChild("order")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    responses.add(data.child("text").getValue().toString());
                }
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

    private String getSharedPreferenceString(String prefKey, String defaultValue) {
        return getActivity().getPreferences(Context.MODE_PRIVATE).getString(prefKey, defaultValue);
    }

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