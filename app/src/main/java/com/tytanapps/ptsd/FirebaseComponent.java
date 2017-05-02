package com.tytanapps.ptsd;

import com.tytanapps.ptsd.facility.FacilitiesFragment;
import com.tytanapps.ptsd.facility.FacilityLoader;
import com.tytanapps.ptsd.fragments.MainFragment;
import com.tytanapps.ptsd.fragments.PTSDTestFragment;
import com.tytanapps.ptsd.fragments.ResourcesFragment;
import com.tytanapps.ptsd.fragments.SettingsFragment;
import com.tytanapps.ptsd.news.NewsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={AppModule.class, FirebaseModule.class})
public interface FirebaseComponent {
    void inject(PTSDApplication application);

    void inject(MainActivity mainActivity);

    void inject(MainFragment mainFragment);
    void inject(PTSDTestFragment ptsdTestFragment);
    void inject(ResourcesFragment resourcesFragment);
    void inject(NewsFragment newsFragment);
    void inject(FacilitiesFragment facilitiesFragment);
    void inject(SettingsFragment settingsFragment);

    void inject(RatingDialog ratingDialog);

    void inject(FacilityLoader facilityLoader);
}