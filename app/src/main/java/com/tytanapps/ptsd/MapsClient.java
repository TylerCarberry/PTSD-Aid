package com.tytanapps.ptsd;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapsClient {

    @GET("streetview/metadata")
    Call<MapsResult> getMapMetadata(@Query("location") String address, @Query("key") String apiKey);

}
