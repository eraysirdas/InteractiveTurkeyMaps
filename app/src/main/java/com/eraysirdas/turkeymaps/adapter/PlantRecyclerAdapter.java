package com.eraysirdas.turkeymaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eraysirdas.turkeymaps.R;
import com.eraysirdas.turkeymaps.model.CityPlantModel;

import java.util.ArrayList;

public class PlantRecyclerAdapter extends RecyclerView.Adapter<PlantRecyclerAdapter.PlantHolder> {

    private final ArrayList<CityPlantModel> cityPlantModelArrayList;
    private final Context context;

    public PlantRecyclerAdapter(ArrayList<CityPlantModel> cityPlantModelArrayList, Context context) {
        this.cityPlantModelArrayList = cityPlantModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public PlantHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plant_row, parent, false);
        return new PlantHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantHolder holder, int position) {

        String baseUrl = "https://flora.biocoder.com.tr/images/plants/";
        String imageUrl = baseUrl + cityPlantModelArrayList.get(position).plantImageUrl; // Modelden gelen imagePath ile birleştir

        Glide.with(context)
                .load(imageUrl)
                .into(holder.rowImageView);

        holder.rowDescriptionTextView.setText(cityPlantModelArrayList.get(position).plantDescription);
        holder.rowHeadTextView.setText(cityPlantModelArrayList.get(position).plantName);

    }

    @Override
    public int getItemCount() {
        return cityPlantModelArrayList.size();
    }

    public static class PlantHolder extends RecyclerView.ViewHolder {
        private ImageView rowImageView;
        private TextView rowDescriptionTextView;
        private TextView rowHeadTextView;
        public PlantHolder(@NonNull View itemView) {
            super(itemView);
            rowImageView=itemView.findViewById(R.id.rowImageView);
            rowDescriptionTextView=itemView.findViewById(R.id.rowDescriptionTv);
            rowHeadTextView=itemView.findViewById(R.id.rowHeadTv);

        }
    }




}
