package com.example.stayhealthy_android_app.Journey;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class JourneyPostViewHolder  extends RecyclerView.ViewHolder  {
    private ImageView postImage;
    private TextView postTextView;
    FirebaseStorage fStorage;
    final long FIVE_MEGABYTE = 1024 * 1024*5;
    public JourneyPostViewHolder(@NonNull View itemView,  FirebaseStorage fStorage) {
        super(itemView);
        this.fStorage = fStorage;
        postTextView = itemView.findViewById(R.id.post_text);
        postImage = itemView.findViewById(R.id.imageViewPost);
    }

    public void bindJourneyPost(JourneyPost post) {
        postTextView.setText(post.getPostStr());
        if (post.getPostPhoto() != null && !post.getPostPhoto().isEmpty()) {
            StorageReference gsReference = fStorage.getReferenceFromUrl(post.getPostPhoto());
            gsReference.getBytes(FIVE_MEGABYTE).addOnSuccessListener((task) -> {
                Bitmap currentImage = BitmapFactory.decodeByteArray(task, 0, task.length);
                this.postImage.setImageBitmap(currentImage);
            });
        }
    }
}
