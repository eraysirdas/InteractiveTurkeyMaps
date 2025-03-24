package com.eraysirdas.turkeymaps.service.api;

import com.eraysirdas.turkeymaps.model.SearchCityResponse;
import com.eraysirdas.turkeymaps.model.SearchDataByTypeResponse;
import com.eraysirdas.turkeymaps.model.MapsModel;
import com.eraysirdas.turkeymaps.model.SearchDataByTypeRequest;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MapsAPI {

    //https://flora.biocoder.com.tr/api/homeapi/city-information

    @GET("city-information")
    Observable<List<MapsModel>> getData();

    @GET("city-detail/{cityName}")
    Call<MapsModel> getDetailData(@Path("cityName") String cityName);

    @POST("search-data-by-type")
    Call<List<SearchDataByTypeResponse>> searchDataByType(@Body SearchDataByTypeRequest searchDataByTypeRequest);

    @POST("search-city")
    Call<List<SearchCityResponse>> searchCity(@Body SearchDataByTypeRequest searchDataByTypeRequest);

}
