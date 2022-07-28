package com.example.stayhealthy_android_app.Period.Model;

import java.util.Objects;

// Period data stored on firebase realtime database.

public class PeriodData implements Comparable<PeriodData>{
    private final String date; // format "mm-dd-yyyy"
    private final String startDate; // format "mm-dd-yyyy"
    private final int hadFlow; // "1", "0"
    private final String flowLevel;
    private final String symptoms;
    private final int mood;

    public PeriodData(String date, String startDate, int hadFlow, String flowLevel, String symptoms, int mood) {
        this.date = date;
        this.startDate = startDate;
        this.hadFlow = hadFlow;
        this.flowLevel = flowLevel;
        this.symptoms = symptoms;
        this.mood = mood;
    }

    public String getDate() {
        return date;
    }

    public String getStartDate() {
        return startDate;
    }

    public int getHadFlow() {
        return hadFlow;
    }

    public String getFlowLevel() {
        return flowLevel;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public int getMood() {
        return mood;
    }

    @Override
    public int compareTo(PeriodData o) {
        return this.getDate().compareTo(o.getDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PeriodData)) return false;
        PeriodData that = (PeriodData) o;
        return getHadFlow() == that.getHadFlow()
                && getMood() == that.getMood()
                && getDate().equals(that.getDate())
                && Objects.equals(getStartDate(), that.getStartDate())
                && Objects.equals(getFlowLevel(), that.getFlowLevel())
                && Objects.equals(getSymptoms(), that.getSymptoms());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getStartDate(), getHadFlow(), getFlowLevel(), getSymptoms(), getMood());
    }
}
