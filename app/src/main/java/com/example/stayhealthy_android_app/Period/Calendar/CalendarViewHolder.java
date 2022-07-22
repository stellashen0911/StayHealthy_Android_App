package com.example.stayhealthy_android_app.Period.Calendar;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView daysOfMonth;
    private final CalendarAdapter.OnItemListener onItemListener;

    public CalendarViewHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener) {
        super(itemView);
        daysOfMonth = itemView.findViewById(R.id.dayCellTextTV);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }

    public TextView getDaysOfMonth() {
        return daysOfMonth;
    }

    @Override
    public void onClick(View v) {
        onItemListener.onItemClick(getAdapterPosition(), daysOfMonth);
    }
}
