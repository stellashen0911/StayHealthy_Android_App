package com.example.stayhealthy_android_app.Journey;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;

public class JourneyPostViewHolder  extends RecyclerView.ViewHolder  {
    public ImageView postImage;
    public JourneyPostViewHolder(@NonNull View itemView) {
        super(itemView);
        postImage = itemView.findViewById(R.id.imageViewPost);
    }

    public void bindJourneyPost(JourneyPost post) {
        postImage.setImageBitmap(post.getPostRes());
    }
}
