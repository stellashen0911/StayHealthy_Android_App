package com.example.stayhealthy_android_app.Journey;

import android.graphics.Bitmap;

public class JourneyPost {
    String postPhotoUri;
    String post;
    public JourneyPost (String postPhotoUri,String post) {
        this.postPhotoUri = postPhotoUri;
        this.post = post;
        if (postPhotoUri == null) {
           this.postPhotoUri= "";
        }
        if (post == null) {
            this.post = "empty post";
        }


    }
    public String getPostPhoto() {
        return postPhotoUri;
    }

    public String getPostStr() {
        return post;
    }

}
