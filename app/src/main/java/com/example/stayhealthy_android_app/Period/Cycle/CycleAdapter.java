package com.example.stayhealthy_android_app.Period.Cycle;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.Period.Model.Cycle;
import com.example.stayhealthy_android_app.R;

import java.util.List;

public class CycleAdapter extends RecyclerView.Adapter<CycleViewHolder> {
    private final List<Cycle> cycleList;

    public CycleAdapter(List<Cycle> cycleList) {
        this.cycleList = cycleList;
    }

    @NonNull
    @Override
    public CycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CycleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cycle_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CycleViewHolder holder, int position) {
        holder.bingThisData(cycleList.get(position));
    }

    @Override
    public int getItemCount() {
        return cycleList != null ? cycleList.size() : 0;
    }
}
