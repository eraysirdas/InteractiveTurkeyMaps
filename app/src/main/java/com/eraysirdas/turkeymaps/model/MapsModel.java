package com.eraysirdas.turkeymaps.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class MapsModel implements Serializable {

    @SerializedName("name")
    public String name;

    @SerializedName("cityMapImage")
    public String cityMapImage;

    @SerializedName("districtCount")
    public int districtCount;

    @SerializedName("hiveCount")
    public int hiveCount;

    @SerializedName("produceCount")
    public int produceCount;

    @SerializedName("plateCode")
    public String plateCode;

    @SerializedName("areCode")
    public String areCode;

    @SerializedName("area")
    public String area;

    @SerializedName("mapPath")
    public List<String> mapPath;

    @SerializedName("id")
    public String id;

    @SerializedName("createdDate")
    public String createdDate;

    @SerializedName("updatedDate")
    public String updatedDate;

    @SerializedName("cityPlants")
    public List<CityPlantModel> cityPlants;

    @SerializedName("cityHoney")
    public List<CityHoneyModel> cityHoney;


    public String getName() {
        return name;
    }

    public List<CityPlantModel> getCityPlants() {
        return cityPlants;
    }

    public List<CityHoneyModel> getCityHoney() {
        return cityHoney;
    }
}
