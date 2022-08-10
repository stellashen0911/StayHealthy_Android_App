package com.example.stayhealthy_android_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
        Bitmap rotate =  RotateBitmap(currentImage, 90f);
        postImageView.setImageBitmap(rotate);
        user = FirebaseAuth.getInstance().getCurrentUser();
        myDataBase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        fStorage = FirebaseStorage.getInstance();
        storageReference = fStorage.getReference("users").child(user.getUid());;
        postButton.setOnClickListener(v->postToFirebase());
        cancelButton.setOnClickListener(v->onCancelClick());
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void postToFirebase() {
        long currentMillis = System.currentTimeMillis();
        String postText = postEditText.getText().toString();
        final StorageReference fileRef = storageReference.child("posts")
                .child(String.valueOf(currentMillis));
                fileRef.putBytes(currentImageBytes).addOnSuccessListener((task)-> {
                    fileRef.getDownloadUrl().addOnSuccessListener((uriTask -> {
                       String uriImage =   uriTask.toString();
                        JourneyPost postModel = new JourneyPost(uriImage,postText);
                        myDataBase.child("posts")
                                .child(String.valueOf(currentMillis))
                                .setValue(postModel).addOnSuccessListener((taskdb) -> {
                                    Intent intent = new Intent(this, JourneyActivity.class);
                                    startActivity(intent);
                                });
                    }));
                });
        Intent intent = new Intent(this, JourneyActivity.class);
        startActivity(intent);
    }

    public void onCancelClick() {
        Intent intent = new Intent(this,JourneyActivity.class);
        startActivity(intent);
    }
}
