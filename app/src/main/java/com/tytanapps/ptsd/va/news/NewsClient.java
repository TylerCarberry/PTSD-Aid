package com.tytanapps.ptsd.va.news;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface NewsClient {

    @GET("get-va-news")
    Call<List<News>> getVaNews();

}
