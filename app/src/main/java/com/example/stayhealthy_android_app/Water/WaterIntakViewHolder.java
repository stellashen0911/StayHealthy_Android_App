package com.example.stayhealthy_android_app.Water;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;

public class WaterIntakViewHolder   extends RecyclerView.ViewHolder {
    TextView waterOz;
    TextView date;
    ImageView star;
    public WaterIntakViewHolder(@NonNull View itemView) {
        super(itemView);
        waterOz = itemView.findViewById(R.id.water_oz);
        date = itemView.findViewById(R.id.date);
        star = itemView.findViewById(R.id.star);
    }

   public void bindWaterIntake(WaterIntakeModel waterIntake) {
        waterOz.setText(String.valueOf(waterIntake.getWaterOz()));
        date.setText(waterIntake.getDate());
        if(waterIntake.getWaterOz() >= WaterIntakeModel.DAILY_WATER_TARGET_OZ) {
            star.setVisibility(View.VISIBLE);
        } else {
            star.setVisibility(View.GONE);
        }

    }


}
