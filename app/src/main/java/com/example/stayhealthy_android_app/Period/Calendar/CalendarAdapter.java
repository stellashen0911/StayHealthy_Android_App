package com.example.stayhealthy_android_app.Period.Calendar;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private final int selectedDay;
    private final int selectedDayColor;
    private final Drawable selectedDayBackground;

    public CalendarAdapter(ArrayList<String> daysOfMonth, OnItemListener onItemListener,
                           int selectedDay, int selectedDayColor, Drawable selectedDayBackground) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.selectedDay = selectedDay;
        this.selectedDayColor = selectedDayColor;
        this.selectedDayBackground = selectedDayBackground;
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

        if(!daysOfMonth.get(position).equals("")) {
            int day = Integer.parseInt(daysOfMonth.get(position));
            if (day == selectedDay) {
                holder.getDaysOfMonth().setTextColor(selectedDayColor);
                holder.getDaysOfMonth().setBackground(selectedDayBackground);
            }
        }

    }

    @Override
    public int getItemCount() {
        return daysOfMonth != null ? daysOfMonth.size() : 0;
    }

    public interface OnItemListener {
        void onItemClick(int position, TextView dateTV);
    }
}
