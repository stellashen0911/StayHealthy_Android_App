package com.example.stayhealthy_android_app.Journey;

import android.graphics.Bitmap;

public class JourneyPost {
    String postPhotoUri;
    String post;
    String dateStr;
    public JourneyPost (String postPhotoUri,String post,String dateStr) {
        this.postPhotoUri = postPhotoUri;
        this.post = post;
        this.dateStr = dateStr;
        if (postPhotoUri == null) {
           this.postPhotoUri= "";
        }
        if (post == null) {
            this.post = "empty post";
        }
        if (dateStr == null) {
            this.dateStr = "000-00-00";
        }


    }
    public String getPostPhoto() {
        return postPhotoUri;
    }

    public String getPostStr() {
        return post;
    }

    public String getDateStr(){
        return this.dateStr;
    }

}
