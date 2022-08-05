package com.example.stayhealthy_android_app.Diet;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.internal.MaterialCheckable;

public class FoodHolder extends RecyclerView.ViewHolder {
    public TextView foodName;
    public TextView foodInfo;
    public MaterialCheckBox checked;

    public FoodHolder(View foodView, final FoodClickListener listener, FoodCheckedListener foodCheckedListener) {
        super(foodView);

        foodName = foodView.findViewById(R.id.food_name);
        foodInfo = foodView.findViewById(R.id.food_info);
        checked = foodView.findViewById(R.id.food_checkbox);

        foodView.setOnClickListener(v -> {
            if (listener != null) {
                int position = getLayoutPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onFoodClicked(position);
                }
            }
        });

        checked.setOnCheckedChangeListener((v, isChecked) -> {
            checked.setChecked(isChecked);
            int position = getLayoutPosition();
            if (position != RecyclerView.NO_POSITION) {
                foodCheckedListener.onFoodChecked(position, isChecked);
            }
        });
    }
}
