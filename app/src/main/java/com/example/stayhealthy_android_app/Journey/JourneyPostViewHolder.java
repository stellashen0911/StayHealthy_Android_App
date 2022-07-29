package com.example.stayhealthy_android_app.Journey;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;

public class JourneyPostViewHolder  extends RecyclerView.ViewHolder  {
    public TextView postText;
    private Context context;
    public JourneyPostViewHolder(@NonNull View itemView) {
        super(itemView);
        postText = itemView.findViewById(R.id.post_text);
    }

    public void bindJourneyPost(JourneyPost post) {
        postText.setText(post.getPost());
    }
}
