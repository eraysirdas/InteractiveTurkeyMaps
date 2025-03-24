package com.eraysirdas.turkeymaps;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SharedPrefManager {

    public static final String PREF_NAME="com.eraysirdas.turkeymaps";
    public static final String KEY_PLANT_NAMES ="allPlantNames";
    public static final String KEY_PLANT_CITY_MAP ="plantCityMap";
    public static final String KEY_HONEY_NAMES ="allHoneyNames";
    public static final String KEY_HONEY_CITY_MAP ="honeyCityMap";

    private SharedPreferences sharedPreferences;
    private Gson gson;
    Context context;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }


    public void saveData(List<String> allPlantNames, Map<String, Set<String>> plantCityMap,
                         List<String> allHoneyNames, Map<String, Set<String>> honeyCityMap){

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_PLANT_NAMES, gson.toJson(allPlantNames));
        editor.putString(KEY_HONEY_NAMES, gson.toJson(allHoneyNames));

        // Map<String, Set<String>> için
        editor.putString(KEY_PLANT_CITY_MAP, gson.toJson(plantCityMap));
        editor.putString(KEY_HONEY_CITY_MAP, gson.toJson(honeyCityMap));

        editor.apply();

    }

    public Map<String, Object> loadData() {
        Map<String, Object> data = new HashMap<>();

        // List<String> için
        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> allPlantNames = gson.fromJson(sharedPreferences.getString(KEY_PLANT_NAMES, ""), listType);
        List<String> allHoneyNames = gson.fromJson(sharedPreferences.getString(KEY_HONEY_NAMES, ""), listType);

        // Map<String, Set<String>> için
        Type mapType = new TypeToken<Map<String, Set<String>>>() {}.getType();
        Map<String, Set<String>> plantCityMap = gson.fromJson(sharedPreferences.getString(KEY_PLANT_CITY_MAP, ""), mapType);
        Map<String, Set<String>> honeyCityMap = gson.fromJson(sharedPreferences.getString(KEY_HONEY_CITY_MAP, ""), mapType);

        data.put(KEY_PLANT_NAMES, allPlantNames);
        data.put(KEY_PLANT_CITY_MAP, plantCityMap);
        data.put(KEY_HONEY_NAMES, allHoneyNames);
        data.put(KEY_HONEY_CITY_MAP, honeyCityMap);

        return data;
    }
}
