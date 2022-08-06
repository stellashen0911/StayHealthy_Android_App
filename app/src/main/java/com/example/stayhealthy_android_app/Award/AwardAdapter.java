package com.example.stayhealthy_android_app.Award;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.Award.Model.AwardData;
import com.example.stayhealthy_android_app.Award.Model.AwardDisplay;

import java.util.List;

public class AwardAdapter extends RecyclerView.Adapter<AwardViewHolder> {
    private final List<AwardDisplay> awardDisplayList;
    private final int itemLayoutID;

    public AwardAdapter(List<AwardDisplay> awardDisplayList, int itemLayoutID) {
        this.awardDisplayList = awardDisplayList;
        this.itemLayoutID = itemLayoutID;
    }

    @NonNull
    @Override
    public AwardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AwardViewHolder(LayoutInflater.from(parent.getContext()).inflate(itemLayoutID, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AwardViewHolder holder, int position) {
        holder.bindThisData(awardDisplayList.get(position));
    }

    @Override
    public int getItemCount() {
        return awardDisplayList != null ? awardDisplayList.size() : 0;
    }
}
