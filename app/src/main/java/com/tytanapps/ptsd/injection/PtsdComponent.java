package com.tytanapps.ptsd.injection;

import com.tytanapps.ptsd.MainActivity;
import com.tytanapps.ptsd.PTSDApplication;
import com.tytanapps.ptsd.dialog.RatingDialog;
import com.tytanapps.ptsd.va.facility.FacilitiesFragment;
import com.tytanapps.ptsd.va.facility.FacilityLoader;
import com.tytanapps.ptsd.fragments.BaseFragment;
import com.tytanapps.ptsd.fragments.MainFragment;
import com.tytanapps.ptsd.fragments.PTSDTestFragment;
import com.tytanapps.ptsd.fragments.ResourcesFragment;
import com.tytanapps.ptsd.settings.SettingsFragment;
import com.tytanapps.ptsd.va.news.NewsFragment;
import com.tytanapps.ptsd.va.news.NewsLoader;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={AppModule.class, PtsdModule.class})
public interface PtsdComponent {
    void inject(PTSDApplication application);

    void inject(MainActivity mainActivity);

    void inject(BaseFragment baseFragment);
    void inject(MainFragment mainFragment);
    void inject(PTSDTestFragment ptsdTestFragment);
    void inject(ResourcesFragment resourcesFragment);
    void inject(NewsFragment newsFragment);
    void inject(FacilitiesFragment facilitiesFragment);
    void inject(SettingsFragment settingsFragment);

    void inject(RatingDialog ratingDialog);

    void inject(FacilityLoader facilityLoader);
    void inject(NewsLoader newsLoader);

}