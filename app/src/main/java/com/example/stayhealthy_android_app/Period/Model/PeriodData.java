package com.example.stayhealthy_android_app.Period.Model;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Period data stored on firebase realtime database.

public class PeriodData implements Comparable<PeriodData>{
    private String date; // format "yyyy-mm-dd"
    private String startDate; // format "yyyy-mm-dd"
    private String endDate; // format "yyyy-mm-dd"
    private boolean hadFlow;
    private String flowLevel;
    private String symptoms;
    private int mood;

    public PeriodData() {
    }

    public PeriodData(String date, String startDate, String endDate, boolean hadFlow, String flowLevel, String symptoms, int mood) {
        this.date = date;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hadFlow = hadFlow;
        this.flowLevel = flowLevel;
        this.symptoms = symptoms;
        this.mood = mood;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("hadFlow", hadFlow);
        result.put("flowLevel", flowLevel);
        result.put("symptoms", symptoms);
        result.put("mood", mood);

        return result;
    }

    public String getDate() {
        return date;
    }

    public String getStartDate() {
        return startDate;
    }

    public boolean getHadFlow() {
        return hadFlow;
    }

    public String getEndDate() {
        return endDate;
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
                && Objects.equals(getEndDate(), that.getEndDate())
                && Objects.equals(getFlowLevel(), that.getFlowLevel())
                && Objects.equals(getSymptoms(), that.getSymptoms());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getStartDate(), getEndDate(), getHadFlow(), getFlowLevel(), getSymptoms(), getMood());
    }

    @NonNull
    @Override
    public String toString() {
        return "Date: " + getDate() + " StartDate: " + getStartDate() + " EndDate: " + getEndDate() + " Had Flow: " + getHadFlow()
                + " Flow Level: " + getFlowLevel() + " Symptoms: " + getSymptoms() + " Mood: " + getMood();
    }
}
