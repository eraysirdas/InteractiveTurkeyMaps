package com.eraysirdas.turkeymaps.service;

import com.eraysirdas.turkeymaps.model.MapsModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MapsAPI {

    //https://flora.biocoder.com.tr/api/homeapi/city-information

    @GET("city-information")
    Call<List<MapsModel>> getData();

    @GET("city-detail/{cityName}")
    Call<MapsModel> getDetailData(@Path("cityName") String cityName);
}
