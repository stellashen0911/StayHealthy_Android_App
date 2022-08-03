package com.example.stayhealthy_android_app.Journey;

import android.graphics.Bitmap;

public class JourneyPost {
    Bitmap post;
    public JourneyPost (Bitmap post) {
        this.post = post;
    }
    public Bitmap getPostRes() {
        return post;
    }
}
