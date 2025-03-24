package com.eraysirdas.turkeymaps.service.repository;

import com.eraysirdas.turkeymaps.model.CityHoneyModel;
import com.eraysirdas.turkeymaps.model.MapsModel;
import com.eraysirdas.turkeymaps.model.SearchDataByTypeRequest;
import com.eraysirdas.turkeymaps.model.SearchDataByTypeResponse;
import com.eraysirdas.turkeymaps.service.api.MapsAPI;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsRepository {

    private final MapsAPI mapsAPI;
    SearchDataByTypeRequest request = new SearchDataByTypeRequest("","","");

    public MapsRepository(MapsAPI mapsAPI){
        this.mapsAPI=mapsAPI;
    }

    public Observable<List<MapsModel>> getMapsData(){
        return mapsAPI.getData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
