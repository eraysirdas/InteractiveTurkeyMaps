package com.eraysirdas.turkeymaps.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.eraysirdas.turkeymaps.R;
import com.eraysirdas.turkeymaps.fragment.PlantFragment;
import com.eraysirdas.turkeymaps.fragment.TabPagerAdapter;
import com.eraysirdas.turkeymaps.model.CityPlantModel;
import com.eraysirdas.turkeymaps.model.MapsModel;
import com.eraysirdas.turkeymaps.service.MapsAPI;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailsActivity extends AppCompatActivity {

    TextView areaTv,districtCountsTv,hiveCountTv,producerCountTv;
    ImageView imageView;
    ArrayList<MapsModel> arrayList;
    private String BASE_URL ="https://flora.biocoder.com.tr/api/homeapi/";
    MapsModel mapsModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mapsModel = (MapsModel) getIntent().getSerializableExtra("model");

        areaTv = findViewById(R.id.areaTv);
        districtCountsTv = findViewById(R.id.districtCountsTv);
        hiveCountTv = findViewById(R.id.hiveCountTv);
        producerCountTv = findViewById(R.id.producerCountTv);
        imageView = findViewById(R.id.imageView);


        areaTv.setText("Bölge: "+mapsModel.area);
        districtCountsTv.setText("İlçe Sayısı: "+mapsModel.districtCount);
        hiveCountTv.setText("Kovan Sayısı: "+mapsModel.hiveCount);
        producerCountTv.setText("Üretici Sayısı: "+ mapsModel.produceCount);


        ImageView imageView = findViewById(R.id.imageView);

        String baseUrl = "https://flora.biocoder.com.tr/images/imageprovinces/";
        String imageUrl = baseUrl + mapsModel.cityMapImage; // Modelden gelen imagePath ile birleştir

        Glide.with(this)
                .load(imageUrl)
                .into(imageView);

        Objects.requireNonNull(getSupportActionBar()).setTitle(mapsModel.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabPagerAdapter adapter = new TabPagerAdapter(this,mapsModel.name);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Bitkiler");
            } else {
                tab.setText("Ballar");
            }
        }).attach();
    }



    /*private void loadData() {
        MapsAPI mapsAPI = retrofit.create(MapsAPI.class);

        Call<MapsModel> call = mapsAPI.getDetailData(mapsModel.name);
        System.out.println(mapsModel.name);

        call.enqueue(new Callback<MapsModel>() {
            @Override
            public void onResponse(Call<MapsModel> call, Response<MapsModel> response) {
                if (response.isSuccessful()) {
                    MapsModel responseList = response.body();
                    if (responseList != null) {
                        arrayList = new ArrayList<>();
                        arrayList.add(responseList);

                        for (MapsModel model : arrayList) {
                            List<CityPlantModel> cityPlantModels = model.getCityPlants();
                            if (cityPlantModels != null && !cityPlantModels.isEmpty()) {
                                for (CityPlantModel plantModel : cityPlantModels) {
                                    System.out.println("Bitki Adı: " + plantModel.plantName);
                                }
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
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
