package com.example.stayhealthy_android_app.Award.Model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class AwardData implements Comparable<AwardData> {
    private String date; // the award data update date
    private String name;
    private int times;

    public AwardData(){

    }

    public AwardData(String date, String name, int times) {
        this.date = date;
        this.name = name;
        this.times = times;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public int getTimes() {
        return times;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void addTimes() {
        this.times += 1;
    }

    @Override
    public int compareTo(AwardData o) {
        return this.getDate().compareTo(o.getDate());
    }

    @NonNull
    @Override
    public String toString() {
        return  "Date: " + getDate() + " Awards: " + getName() + " Details: " + getTimes();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AwardData)) return false;
        AwardData data = (AwardData) o;
        return getTimes() == data.getTimes()
                && getDate().equals(data.getDate()) && getName().equals(data.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getName(), getTimes());
    }
}
