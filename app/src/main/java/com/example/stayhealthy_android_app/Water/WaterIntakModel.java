package com.example.stayhealthy_android_app.Water;

public class WaterIntakModel {
    int waterOz;
    String date;
    public static final int DAILY_WATER_TARGET_OZ = 64;

    public WaterIntakModel(int waterOz, String date) {
        this.waterOz = waterOz;
        this.date = date;
    }

    public void addWater(int waterAddedOz) {
        waterOz += waterAddedOz;
    }

    public int getWaterOz() {
        return waterOz;
    }

    public String getDate() {
        return date;
    }
}
