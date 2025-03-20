package com.eraysirdas.turkeymaps.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eraysirdas.turkeymaps.R;
import com.eraysirdas.turkeymaps.adapter.PlantRecyclerAdapter;
import com.eraysirdas.turkeymaps.model.CityPlantModel;
import com.eraysirdas.turkeymaps.model.MapsModel;
import com.eraysirdas.turkeymaps.service.MapsAPI;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PlantFragment extends Fragment {
    private RecyclerView recyclerView;
    ShimmerFrameLayout shimmerFrameLayout;
    private ArrayList<CityPlantModel> recyclerDataArrayList;
    ArrayList<MapsModel> arrayList;
    private String BASE_URL ="https://flora.biocoder.com.tr/api/homeapi/";
    Retrofit retrofit;
    private String cityName;
    private boolean isDataLoaded = false;



    public PlantFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView=view.findViewById(R.id.plantRecyclerView);
        shimmerFrameLayout=view.findViewById(R.id.shimmerView);


        Gson gson = new GsonBuilder().setLenient().create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        if (getArguments() != null) {
            cityName = getArguments().getString("cityName");
        }

        //System.out.println("Şehir Adı: " + cityName);


        if (!isDataLoaded) {
            showPlaceholder();
            loadData();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
        }

        //loadData();

        recyclerDataArrayList = new ArrayList<>();
        PlantRecyclerAdapter adapter=new PlantRecyclerAdapter(recyclerDataArrayList,requireContext());
        GridLayoutManager layoutManager=new GridLayoutManager(requireContext(),2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

    }

    private void showPlaceholder() {
        shimmerFrameLayout.startShimmer();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void loadData() {
        MapsAPI mapsAPI = retrofit.create(MapsAPI.class);

        Call<MapsModel> call = mapsAPI.getDetailData(cityName);


        call.enqueue(new Callback<MapsModel>() {
            @Override
            public void onResponse(Call<MapsModel> call, Response<MapsModel> response) {
                if (response.isSuccessful()) {
                    MapsModel responseList = response.body();
                    if (responseList != null) {
                        arrayList = new ArrayList<>();
                        arrayList.add(responseList);

                        recyclerDataArrayList.clear();

                        for (MapsModel model : arrayList) {
                            List<CityPlantModel> cityPlantModels = model.getCityPlants();
                            if (cityPlantModels != null && !cityPlantModels.isEmpty()) {

                                recyclerDataArrayList.addAll(cityPlantModels);
                                recyclerView.getAdapter().notifyDataSetChanged();


                                isDataLoaded = true; // Veriler yüklendi
                                stopPlaceHolder();

                                /*for (CityPlantModel plantModel : cityPlantModels) {
                                    System.out.println("Bitki Adı: " + plantModel.plantName);
                                }*/
                            } else {
                                System.out.println("CityPlants listesi boş veya null.");
                            }
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<MapsModel> call, Throwable t) {

                System.out.println("API çağrısı başarısız. Hata: " + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void stopPlaceHolder() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        },1000);
    }

}