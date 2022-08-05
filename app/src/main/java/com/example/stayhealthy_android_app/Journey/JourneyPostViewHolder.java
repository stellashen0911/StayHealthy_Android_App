package com.example.stayhealthy_android_app.Journey;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;

public class JourneyPostViewHolder  extends RecyclerView.ViewHolder  {
    public ImageView postImage;
    public TextView postTextView;
    public JourneyPostViewHolder(@NonNull View itemView) {
        super(itemView);
        postTextView = itemView.findViewById(R.id.post_text);
        postImage = itemView.findViewById(R.id.imageViewPost);
    }

    public void bindJourneyPost(JourneyPost post) {
//        postImage.setImageBitmap(post.());
        postTextView.setText(post.getPostStr());
    }
}
