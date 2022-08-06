package com.example.stayhealthy_android_app.Award;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.Award.Model.AwardDisplay;
import com.example.stayhealthy_android_app.R;

public class AwardViewHolder extends RecyclerView.ViewHolder{
    private final TextView awardNameTV;
    private final TextView awardDetailsTV;

    public AwardViewHolder(@NonNull View itemView) {
        super(itemView);

        this.awardNameTV = itemView.findViewById(R.id.awardNameTV);
        this.awardDetailsTV = itemView.findViewById(R.id.awardDetailsTV);
    }

    public void bindThisData(AwardDisplay awardDisplayToBind) {
        awardNameTV.setText(awardDisplayToBind.getName());
        awardDetailsTV.setText(awardDisplayToBind.getDetails());
    }
}
