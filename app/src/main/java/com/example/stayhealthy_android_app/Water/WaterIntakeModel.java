package com.example.stayhealthy_android_app.Water;

import androidx.annotation.NonNull;

public class WaterIntakeModel {
    long waterOz;
    String date;
    public static final long DAILY_WATER_TARGET_OZ = 64;

    public WaterIntakeModel() {

    }

    public WaterIntakeModel(long waterOz, String date) {
        this.waterOz = waterOz;
        this.date = date;
    }
    public void addWater(long waterAddedOz) {
        waterOz += waterAddedOz;
    }

    public long getWaterOz() {
        return waterOz;
    }

    public String getDate() {
        return date;
    }

    @NonNull
    @Override
    public String toString() {
        return "Date: " + getDate() + " WaterOz: " + getWaterOz();
    }
}
