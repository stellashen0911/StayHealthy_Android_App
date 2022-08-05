package com.example.stayhealthy_android_app.Award.Model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class AwardData implements Comparable<AwardData> {
    private String date;
    private String name;
    private int times;
    private String details;
    private int type;

    public AwardData(){

    }

    public AwardData(String date, String name, int times, String details, int type) {
        this.date = date;
        this.name = name;
        this.times = times;
        this.details = details;
        this.type = type;
    }

    public void setDetails(String details) {
        this.details = details;
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

    public void setType(int type) {
        this.type = type;
    }

    public String getDetails() {
        return details;
    }

    public int getType() {
        return type;
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
        return getType() == data.getType() && getTimes() == data.getTimes()
                && getDate().equals(data.getDate()) && getName().equals(data.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getDate(), getName(), getTimes());
    }
}
