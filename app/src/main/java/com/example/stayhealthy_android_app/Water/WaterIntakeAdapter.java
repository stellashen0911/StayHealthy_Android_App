package com.example.stayhealthy_android_app.Water;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.stayhealthy_android_app.Journey.JourneyPostViewHolder;
import com.example.stayhealthy_android_app.R;

import java.util.ArrayList;
import java.util.List;


public class WaterIntakeAdapter extends RecyclerView.Adapter<WaterIntakViewHolder> {
    private final List<WaterIntakModel> waterIntakes;
    private final Context context;
    public WaterIntakeAdapter ( List<WaterIntakModel> waterIntakes, Context context) {
        this.waterIntakes = waterIntakes;
        this.context = context;
    }

    @NonNull
    @Override
    public WaterIntakViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return new WaterIntakViewHolder(layoutInflater.inflate(R.layout.water_intake_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull WaterIntakViewHolder holder, int position) {
        holder.bindWaterIntake(waterIntakes.get(position));
    }

    @Override
    public int getItemCount() {
        return waterIntakes.size();
    }
}
