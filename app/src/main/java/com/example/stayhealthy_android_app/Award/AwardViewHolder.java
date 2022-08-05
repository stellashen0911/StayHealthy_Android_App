package com.example.stayhealthy_android_app.Award;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.Award.Model.AwardData;
import com.example.stayhealthy_android_app.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AwardViewHolder extends RecyclerView.ViewHolder{
    private final static String DATE_SHORT_FORMAT = "yyyy-MM-dd";
    private final TextView awardNameTV;
    private final TextView awardDetailsTV;

    public AwardViewHolder(@NonNull View itemView) {
        super(itemView);

        this.awardNameTV = itemView.findViewById(R.id.awardNameTV);
        this.awardDetailsTV = itemView.findViewById(R.id.awardDetailsTV);
    }

    public void bindThisData(AwardData awardDataToBind) {
        awardNameTV.setText(awardDataToBind.getName());
        awardDetailsTV.setText(awardDataToBind.getDetails());
    }
}
