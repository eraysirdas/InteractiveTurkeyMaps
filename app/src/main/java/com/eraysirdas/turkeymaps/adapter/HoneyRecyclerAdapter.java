package com.eraysirdas.turkeymaps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eraysirdas.turkeymaps.R;
import com.eraysirdas.turkeymaps.model.CityHoneyModel;
import com.eraysirdas.turkeymaps.model.CityPlantModel;

import java.util.ArrayList;


public class HoneyRecyclerAdapter extends RecyclerView.Adapter<HoneyRecyclerAdapter.HoneyHolder> {
    private final ArrayList<CityHoneyModel> cityHoneyModelArrayList;

    public HoneyRecyclerAdapter(ArrayList<CityHoneyModel> cityHoneyModelArrayList) {
        this.cityHoneyModelArrayList = cityHoneyModelArrayList;
    }

    @NonNull
    @Override
    public HoneyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.honey_row, parent, false);
        return new HoneyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HoneyHolder holder, int position) {
        holder.honeyTextView.setText(cityHoneyModelArrayList.get(position).honeyVerietyName);
    }

    @Override
    public int getItemCount() {
        return cityHoneyModelArrayList.size();
    }

    public class HoneyHolder extends RecyclerView.ViewHolder {
        private TextView honeyTextView;
        public HoneyHolder(@NonNull View itemView) {
            super(itemView);
            honeyTextView=itemView.findViewById(R.id.honeyRowTv);
        }
    }
}
