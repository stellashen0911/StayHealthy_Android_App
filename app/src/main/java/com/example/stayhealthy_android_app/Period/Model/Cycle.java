package com.example.stayhealthy_android_app.Period.Model;

import androidx.annotation.NonNull;

public class Cycle implements Comparable<Cycle>{
    private final String startDate;
    private final String cycleRanges;
    private final String totalDays;

    public Cycle(String startDate, String cycleRanges, String totalDays) {
        this.startDate = startDate;
        this.cycleRanges = cycleRanges;
        this.totalDays = totalDays;
    }

    public String getCycleRanges() {
        return cycleRanges;
    }

    public String getTotalDays() {
        return totalDays;
    }

    public String getStartDate() {
        return startDate;
    }

    @Override
    public int compareTo(Cycle o) {
        return this.getStartDate().compareTo(o.getStartDate());
    }

    @NonNull
    @Override
    public String toString() {
        return "Cycles: " + getCycleRanges() + " totalDays: " + totalDays;
    }
}
