package com.example.stayhealthy_android_app.Water;

public class WaterIntakeModel {
    long waterOz;
    String date;
    public static final long DAILY_WATER_TARGET_OZ = 64;

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


}
