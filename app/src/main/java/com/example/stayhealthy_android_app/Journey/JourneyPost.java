package com.example.stayhealthy_android_app.Journey;

import android.graphics.Bitmap;

public class JourneyPost {
    Bitmap postPhoto;
    String post;
    public JourneyPost (Bitmap postPhoto,String post) {
        this.postPhoto = postPhoto;
        this.post = post;
    }
    public Bitmap getPostPhoto() {
        return postPhoto;
    }

    public String getPostStr() {
        return post;
    }

}
