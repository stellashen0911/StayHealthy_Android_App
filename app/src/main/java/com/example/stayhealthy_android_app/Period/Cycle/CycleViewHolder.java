package com.example.stayhealthy_android_app.Period.Cycle;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.Period.Model.Cycle;
import com.example.stayhealthy_android_app.R;

public class CycleViewHolder extends RecyclerView.ViewHolder{
    private final TextView startDateTV;
    private final TextView endDateTV;
    private final TextView cycleRangeTV;

    public CycleViewHolder(@NonNull View itemView) {
        super(itemView);

        this.startDateTV = itemView.findViewById(R.id.startDateTV);
        this.endDateTV = itemView.findViewById(R.id.endDateTV);
        this.cycleRangeTV = itemView.findViewById(R.id.cycleRangeTV);
    }

    public void bingThisData(Cycle cycleToBing) {
        startDateTV.setText(cycleToBing.getStartDate());
        endDateTV.setText(cycleToBing.getEndDate());
        cycleRangeTV.setText(cycleToBing.getCycleRanges());
    }


}
