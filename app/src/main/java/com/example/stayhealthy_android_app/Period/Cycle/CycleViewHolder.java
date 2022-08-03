package com.example.stayhealthy_android_app.Period.Cycle;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.Period.Model.Cycle;
import com.example.stayhealthy_android_app.R;

public class CycleViewHolder extends RecyclerView.ViewHolder{
    private final TextView cycleRangeTV;
    private final TextView totalDaysTV;

    public CycleViewHolder(@NonNull View itemView) {
        super(itemView);

        this.cycleRangeTV = itemView.findViewById(R.id.cycleRangeTV);
        this.totalDaysTV = itemView.findViewById(R.id.totalDaysTV);
    }

    public void bingThisData(Cycle cycleToBing) {
        cycleRangeTV.setText(cycleToBing.getCycleRanges());
        totalDaysTV.setText(cycleToBing.getTotalDays());
    }
}
