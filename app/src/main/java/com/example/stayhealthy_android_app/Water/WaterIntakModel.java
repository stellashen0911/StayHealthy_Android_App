package com.example.stayhealthy_android_app.Water;

public class WaterIntakModel {
    int waterOz;
    long timestamp;
    public static final int DAILY_WATER_TARGET_OZ = 64;
    public WaterIntakModel (int waterOz, long timestamp) {
        this.waterOz = waterOz;
        this.timestamp = timestamp;
    }

    public void addWater (int waterAddedOz) {
        waterOz+=waterAddedOz;
    }
}
