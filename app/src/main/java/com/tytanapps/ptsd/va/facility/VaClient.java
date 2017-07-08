package com.tytanapps.ptsd.va.facility;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface VaClient {

    @GET("get-va-facilities")
    Call<List<Facility>> getVaFacilities();

}