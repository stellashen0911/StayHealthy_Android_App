package com.example.stayhealthy_android_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stayhealthy_android_app.Journey.JourneyPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;

public class EditPostActivity  extends AppCompatActivity {

    ImageView postImageView;
    EditText postEditText;
    Button postButton;
    Button cancelButton;
    FirebaseUser user;
    private DatabaseReference myDataBase;
    FirebaseStorage fStorage;
    StorageReference storageReference;
    Bitmap currentImage;
    byte [] currentImageBytes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        postImageView =  findViewById(R.id.post_image);
        postEditText = findViewById(R.id.post_text);
        postButton = findViewById(R.id.post_button);
        cancelButton = findViewById(R.id.cancel);
        currentImageBytes = getIntent().getByteArrayExtra("image");
        if(currentImageBytes == null ) {
            onBackPressed();
        }
        currentImage= BitmapFactory.decodeByteArray(currentImageBytes, 0, currentImageBytes.length);
        postImageView.setImageBitmap(currentImage);
        user = FirebaseAuth.getInstance().getCurrentUser();
        myDataBase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        fStorage = FirebaseStorage.getInstance();
        storageReference = fStorage.getReference("users").child(user.getUid());;
        postButton.setOnClickListener(v->postToFirebase());
        cancelButton.setOnClickListener(v->onCancelClick());
    }


    public void postToFirebase() {
        long currentMillis = System.currentTimeMillis();
        myDataBase.child("posts")
                .child(String.valueOf(currentMillis))
                .child("post_text")
                .setValue(postEditText.getText().toString());
        storageReference.child("posts")
                .child(String.valueOf(currentMillis))
                .child("post_image")
                .putBytes(currentImageBytes);
        try {
            storageReference.child("posts")
                    .child(String.valueOf(currentMillis))
                    .child("post_text")
                    .putBytes(
                            postEditText.getText()
                            .toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, JourneyActivity.class);
        startActivity(intent);
    }

    public void onCancelClick() {
        Intent intent = new Intent(this,JourneyActivity.class);
        startActivity(intent);
    }


}
