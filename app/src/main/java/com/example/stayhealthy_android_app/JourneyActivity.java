package com.example.stayhealthy_android_app;

import static com.example.stayhealthy_android_app.EditPostActivity.RotateBitmap;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stayhealthy_android_app.Journey.JourneyPost;
import com.example.stayhealthy_android_app.Journey.JourneyPostAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class JourneyActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;
    private static final String POST_DB_NAME = "posts";
    RecyclerView postRecyclerView;
    JourneyPostAdapter postAdapter;
    private List<JourneyPost> posts;
    // set a new activity on this button to open camera
    FloatingActionButton addButton;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView profile_nv;
    FirebaseUser user;
    private DatabaseReference myDataBase;
    FirebaseStorage fStorage;
    StorageReference storageReference;

    //new to add
    static final int REQUEST_IMAGE = 100;
    ImageView user_image;
    // Uri indicates, where the image will be picked from
    private Uri filePath;
    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storage_reference;
    byte [] currentImageBytes;
    final long FIVE_MEGABYTE = 1024 * 1024 * 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addButton = findViewById(R.id.add_new_post);
        setContentView(R.layout.activity_journey);

        fStorage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = fStorage.getReference("users").child(user.getUid());

        posts = new ArrayList<>();
        postRecyclerView = findViewById(R.id.post_recycler_view);
        postRecyclerView.setHasFixedSize(false);
        postAdapter  = new JourneyPostAdapter(posts, this,fStorage);
        postRecyclerView.setAdapter(postAdapter);
        postRecyclerView .setLayoutManager(new LinearLayoutManager(this));
        myDataBase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        myDataBase.child(POST_DB_NAME).get().addOnCompleteListener((task) -> {
            HashMap<String, HashMap<String,String>> tempMap = (HashMap) task.getResult().getValue();
            if (tempMap == null ) {
                tempMap = new HashMap<>();
            }
            List<String> timestamps = new ArrayList<>(tempMap.keySet() );
            timestamps.sort((a, b) -> {
                if (Long.valueOf(a) - Long.valueOf(b) > 0) {
                    return -1;
                } else {
                    return 1;
                }
            });
            for (int i = 0; i < timestamps.size(); i++) {
                String postStr = tempMap.get(timestamps.get(i)).get("postStr");
                String post_photo = tempMap.get(timestamps.get(i)).get("postPhoto");
                Date currentDate = new Date(Long.valueOf(timestamps.get(i)));
                posts.add(new JourneyPost(post_photo,postStr,convertDateToDateStr(currentDate)));
                postAdapter.notifyDataSetChanged();
            }
        });
        // Initialize and assign variable
        initWidgets();
        setBottomNavigationView();
        initProfileDrawer();
        setBottomNavigationView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.journey_icon);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void initWidgets() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
    }

    private void initProfileDrawer() {
        // Initialize profile drawer
        drawer = findViewById(R.id.drawer_layout);
        profile_nv = findViewById(R.id.nav_view_journey);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Journey");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        profile_nv.setNavigationItemSelectedListener(this);

        //set up the header button listeners
        View headerView = profile_nv.getHeaderView(0);
        Button LogOutBtn = (Button) headerView.findViewById(R.id.profile_logout_btn);
        Button ChangeAvartaButton = (Button) headerView.findViewById(R.id.update_profile_image_btn);
        TextView userNameText = (TextView) headerView.findViewById(R.id.user_name_show);
        user_image = (ImageView) headerView.findViewById(R.id.image_avatar);
        storage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage_reference = storage.getReference("users").child(user.getUid());

        // calling add value event listener method
        // for getting the values from database.
        DatabaseReference email_ref = myDataBase.child("email");

        email_ref.get().addOnCompleteListener(task -> {
            try {
                String email = (String) task.getResult().getValue();
                userNameText.setText(email);
            } catch (Exception err) {
                System.out.println("error retreive data from database");
            }
        });

        LogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        update_image_from_database();
        ChangeAvartaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddPicturePressed_profile(v);
            }
        });
    }

    //new to add
    public void update_image_from_database() {
        //if the firebase already has an image for the profile, use the existing one
        myDataBase.child("profile_image").get().addOnCompleteListener((task) -> {
            String tempStr = (String) task.getResult().getValue();
            if (tempStr == null ) {
                tempStr = "";
            }
            if (!tempStr.equals("")) {
                //update the image
                StorageReference profileReference = storage.getReferenceFromUrl(tempStr);
                profileReference.getBytes(FIVE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap currentImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Bitmap rotate =  RotateBitmap(currentImage, 90f);
                        user_image.setImageBitmap(rotate);
                    }
                });
            }
        });
    }

    //new to add
    public void onAddPicturePressed_profile(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE);
        } catch (ActivityNotFoundException e) {
            // simply return to the last activity.
            onBackPressed();
        }
    }

    public void upload_image_to_cloud() {
        long currentMillis = System.currentTimeMillis();
        final StorageReference fileRef = storage_reference.child("profile_avatar")
                .child(String.valueOf(currentMillis));
        fileRef.putBytes(currentImageBytes).addOnSuccessListener((task)-> {
            fileRef.getDownloadUrl().addOnSuccessListener((uriTask -> {
                String uriImage = uriTask.toString();
                myDataBase.child("profile_image").setValue(uriImage);
            }));
        });
    }

    private void setBottomNavigationView() {
        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.journey_icon);

        // Perform item selected listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int selectedId = item.getItemId();
            boolean isItemSelected = false;
            if(selectedId == R.id.award_icon) {
                startActivity(new Intent(getApplicationContext(), AwardActivity.class));
                overridePendingTransition(0,0);
                isItemSelected = true;
            } else if (selectedId == R.id.health_record_icon) {
                startActivity(new Intent(getApplicationContext(), HealthRecordActivity.class));
                overridePendingTransition(0, 0);
                isItemSelected = true;
            } else if (selectedId == R.id.journey_icon) {
                isItemSelected = true;
            }
            return isItemSelected;
        });
    }

    public void onAddPicturePressed(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // simply return to the last activity.
            onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Intent editPostIntent = new Intent(this,EditPostActivity.class);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            editPostIntent.putExtra("image",byteArray);
            startActivity(editPostIntent);
        }
        else if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            filePath = data.getData();
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            currentImageBytes = byteArray;
            Bitmap currentImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            Bitmap rotate =  RotateBitmap(currentImage, 90f);
            user_image.setImageBitmap(rotate);
            upload_image_to_cloud();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        switch(item.getItemId()) {
            case R.id.nav_settings:
                drawer.closeDrawers();
                Intent i = new Intent(JourneyActivity.this, SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.nav_health_records:
                drawer.closeDrawers();
                startActivity(new Intent(getApplicationContext(), HealthRecordActivity.class));
                break;
            case R.id.nav_award:
                drawer.closeDrawers();
                startActivity(new Intent(getApplicationContext(), AwardActivity.class));
                break;
            case R.id.nav_journey:
                drawer.closeDrawers();
                startActivity(new Intent(getApplicationContext(), JourneyActivity.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Convert date to date in string
    private String convertDateToDateStr(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(/*dateFormat=*/"yyyy-MM-dd", Locale.US);
        return format.format(date);
    }
}