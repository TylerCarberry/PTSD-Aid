package com.tytanapps.ptsd.va.facility.maps;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapsClient {

    @GET("get-facility-image")
    Call<MapsResult> getFacilityImage(@Query("facility_id") String facilityId);

}
