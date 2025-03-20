package com.eraysirdas.turkeymaps.model;

import com.google.gson.annotations.SerializedName;

public class CityPlantModel {

    @SerializedName("id")
    public String id;

    @SerializedName("cityName")
    public String cityName;

    @SerializedName("plantName")
    public String plantName;

    @SerializedName("plantDescription")
    public String plantDescription;

    @SerializedName("plantImageUrl")
    public String plantImageUrl;

    @SerializedName("area")
    public String area;

    @SerializedName("createdDate")
    public String createdDate;

    @SerializedName("updatedDate")
    public String updatedDate;

}
