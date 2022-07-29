package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.example.stayhealthy_android_app.Journey.JourneyPost;
import com.example.stayhealthy_android_app.Journey.JourneyPostAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class JourneyActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private DatabaseReference myDataBase;
    private static final String POST_DB_NAME = "posts";
    RecyclerView postRecyclerView;
    private List<JourneyPost> posts;
    // set a new activity on this button to open camera
    FloatingActionButton addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addButton = findViewById(R.id.add_new_post);
        setContentView(R.layout.activity_journey);
        // Initialize and assign variable
        initWidgets();
        posts = new ArrayList<>();
        postRecyclerView = findViewById(R.id.post_recycler_view);
        postRecyclerView.setHasFixedSize(false);
        JourneyPostAdapter postAdapter  = new JourneyPostAdapter(posts, this);
        postRecyclerView.setAdapter(postAdapter);
        postRecyclerView .setLayoutManager(new LinearLayoutManager(this));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myDataBase = FirebaseDatabase.getInstance().getReference();
        Calendar cal = Calendar.getInstance();
        String userDBName = user.getEmail().replace('.','_');
        // demo code remove
        myDataBase.child("user").child(userDBName).child(POST_DB_NAME).removeValue();
        myDataBase.child("user").child(userDBName).child(POST_DB_NAME).child(String.valueOf(cal.getTimeInMillis())).setValue(new JourneyPost("this is a post"));
       // end of demo code
        myDataBase.child("user").child(userDBName).child(POST_DB_NAME).get().addOnCompleteListener((task) -> {
            HashMap<String, HashMap<String,String>> tempMap = (HashMap) task.getResult().getValue();
            List<String> timestamps = new ArrayList<>(tempMap.keySet());
            timestamps.sort((a, b) -> {
                if (Long.valueOf(a) - Long.valueOf(b) > 0) {
                    return 1;
                } else {
                    return -1;
                }
            });
            for (int i = 0; i < timestamps.size(); i++) {
                String postStr = tempMap.get(timestamps.get(i)).get("post");
                posts.add(new JourneyPost(postStr));
                postAdapter.notifyDataSetChanged();
            }
        });
        setBottomNavigationView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.journey_icon);
    }

    private void initWidgets() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
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
}