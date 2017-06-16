package com.tytanapps.ptsd.injection;

import android.content.Context;

import com.tytanapps.ptsd.PTSDApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private PTSDApplication ptsdApplication;

    public AppModule(PTSDApplication ptsdApplication) {
        this.ptsdApplication = ptsdApplication;
    }

    @Provides
    @Singleton
    PTSDApplication providesApplication() {
        return ptsdApplication;
    }

    @Provides
    @Singleton
    Context providesContext() {
        return ptsdApplication;
    }

}