package com.example.stayhealthy_android_app.Period.Model;

public class Cycle implements Comparable<Cycle>{
    private String startDate;
    private String endDate;
    private int cycleRanges;

    public Cycle(String startDate, String endDate, int cycleRanges) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.cycleRanges = cycleRanges;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getCycleRanges() {
        return cycleRanges;
    }

    @Override
    public int compareTo(Cycle o) {
        return this.getEndDate().compareTo(o.getEndDate());
    }
}
