package com.example.stayhealthy_android_app.Diet;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;

import java.util.ArrayList;

public class FoodAdapter extends RecyclerView.Adapter<FoodHolder>{
    final private ArrayList<FoodItem> foods;
    private FoodClickListener listener;

    public FoodAdapter(ArrayList<FoodItem> foods) {
        this.foods = foods;
    }

    public void setFoodClickListener(FoodClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FoodHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.food, parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodHolder holder, int position) {
        FoodItem link = foods.get(position);
        holder.foodName.setText(link.getFoodName());
        holder.foodInfo.setText(link.getFoodInfo());
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }
}
