package com.eraysirdas.turkeymaps.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabPagerAdapter extends FragmentStateAdapter {
    private String cityName;

    public TabPagerAdapter(@NonNull FragmentActivity fragmentActivity,String cityName) {
        super(fragmentActivity);
        this.cityName=cityName;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                PlantFragment fragment = new PlantFragment();
                Bundle bundle = new Bundle();
                bundle.putString("cityName", cityName); // Şehir adını Bundle ile gönder
                fragment.setArguments(bundle);
                return fragment;
            }
            case 1: {
                HoneyFragment fragment = new HoneyFragment();
                Bundle bundle = new Bundle();
                bundle.putString("cityName", cityName);
                fragment.setArguments(bundle);
                return fragment;
            }
        }
        return null;
    }


    @Override
    public int getItemCount() {
        return 2;
    }
}
