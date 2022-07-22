package com.example.stayhealthy_android_app.Period.Calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_calender_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.getDaysOfMonth().setText(daysOfMonth.get(position));
    }

    @Override
    public int getItemCount() {
        return daysOfMonth != null ? daysOfMonth.size() : 0;
    }

    public interface OnItemListener {
        void onItemClick(int position, String dayText);
    }
}
