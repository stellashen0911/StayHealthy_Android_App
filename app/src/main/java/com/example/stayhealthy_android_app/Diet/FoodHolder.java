package com.example.stayhealthy_android_app.Diet;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;

public class FoodHolder extends RecyclerView.ViewHolder {
    public TextView foodName;
    public TextView foodInfo;

    public FoodHolder(View foodView, final FoodClickListener listener) {
        super(foodView);

        foodName = foodView.findViewById(R.id.food_name);
        foodInfo = foodView.findViewById(R.id.food_info);

        foodView.setOnClickListener(v -> {
            if (listener != null) {
                int position = getLayoutPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onLinkClicked(position);
                }
            }
        });
    }
}
