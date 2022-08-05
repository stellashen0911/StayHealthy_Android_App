package com.example.stayhealthy_android_app.Award;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.Award.Model.AwardData;

import java.util.List;

public class AwardAdapter extends RecyclerView.Adapter<AwardViewHolder> {
    private final List<AwardData> awardDataList;
    private final int itemLayoutID;

    public AwardAdapter(List<AwardData> awardDataList, int itemLayoutID) {
        this.awardDataList = awardDataList;
        this.itemLayoutID = itemLayoutID;
    }

    @NonNull
    @Override
    public AwardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AwardViewHolder(LayoutInflater.from(parent.getContext()).inflate(itemLayoutID, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AwardViewHolder holder, int position) {
        holder.bindThisData(awardDataList.get(position));
    }

    @Override
    public int getItemCount() {
        return awardDataList != null ? awardDataList.size() : 0;
    }
}
