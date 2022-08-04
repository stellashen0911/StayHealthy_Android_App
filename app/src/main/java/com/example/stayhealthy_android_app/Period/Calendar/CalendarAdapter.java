package com.example.stayhealthy_android_app.Period.Calendar;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {
    private final List<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private final int selectedDate;
    private final int selectedDateColor;
    private final Drawable selectedDateBackground;
    private final List<Integer> periodDates;
    private final int periodDatesColor;
    private final Drawable periodDatesBackground;
    private final List<Integer> recordedDates;
    private final int recordedDatesColor;
    private final Drawable recordedDatesBackground;

    public CalendarAdapter(List<String> daysOfMonth, OnItemListener onItemListener,
                           int selectedDate, int selectedDateColor, Drawable selectedDateBackground,
                           List<Integer> periodDates, int periodDatesColor, Drawable periodDatesBackground,
                           List<Integer> recordedDates, int recordedDatesColor, Drawable recordedDatesBackground) {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.selectedDate = selectedDate;
        this.selectedDateColor = selectedDateColor;
        this.selectedDateBackground = selectedDateBackground;
        this.periodDates = periodDates;
        this.periodDatesColor = periodDatesColor;
        this.periodDatesBackground = periodDatesBackground;
        this.recordedDates = recordedDates;
        this.recordedDatesColor = recordedDatesColor;
        this.recordedDatesBackground = recordedDatesBackground;
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
            if (day == selectedDate && periodDates.contains(day)) {
                LayerDrawable combinedDrawable = new LayerDrawable(new Drawable[] {periodDatesBackground, selectedDateBackground});
                holder.getDaysOfMonth().setTextColor(periodDatesColor);
                holder.getDaysOfMonth().setBackground(combinedDrawable);
            } else if (day == selectedDate) {
                holder.getDaysOfMonth().setTextColor(selectedDateColor);
                holder.getDaysOfMonth().setBackground(selectedDateBackground);
            } else if (periodDates.contains(day)) {
                holder.getDaysOfMonth().setTextColor(periodDatesColor);
                holder.getDaysOfMonth().setBackground(periodDatesBackground);
            } else if (recordedDates.contains(day)) {
                holder.getDaysOfMonth().setTextColor(recordedDatesColor);
                holder.getDaysOfMonth().setBackground(recordedDatesBackground);
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
