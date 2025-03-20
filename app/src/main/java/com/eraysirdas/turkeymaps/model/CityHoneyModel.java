package com.eraysirdas.turkeymaps.model;

import com.google.gson.annotations.SerializedName;

public class CityHoneyModel {
    @SerializedName("id")
    public String id;

    @SerializedName("cityName")
    public String cityName;

    @SerializedName("honeyVerietyName")
    public String honeyVerietyName;

    @SerializedName("area")
    public String area;

    @SerializedName("createdDate")
    public String createdDate;

    @SerializedName("updatedDate")
    public String updatedDate;
}
