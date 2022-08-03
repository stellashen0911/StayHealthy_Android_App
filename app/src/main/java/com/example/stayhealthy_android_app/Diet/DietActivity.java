package com.example.stayhealthy_android_app.Diet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.stayhealthy_android_app.AwardActivity;
import com.example.stayhealthy_android_app.JourneyActivity;
import com.example.stayhealthy_android_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Random;

public class DietActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    private long protein;
    private long fat;
    private long carbs;
    private long netCal;
    private long targetCal;
    private long weight;
    private TextView proteinView;
    private TextView fatView;
    private TextView carbsView;
    private TextView netCalView;
    private TextView targetCalView;
    private TextView weightView;
    private ImageView imageView;

    private DatabaseReference myDataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet);
        myDataBase = FirebaseDatabase.getInstance().getReference("user").
                child("test@gmail_com").child("diets").child("20220731");

        initWidgets();
        setBottomNavigationView();
        initTextViews();
        initImageView();
        loadValues();
    }

    private void loadValues() {
        myDataBase.get().addOnCompleteListener(task -> {
            HashMap tempMap = (HashMap) task.getResult().getValue();
            protein = (long) tempMap.get("protein");
            fat = (long) tempMap.get("fat");
            carbs = (long) tempMap.get("carbs");
            netCal = (long) tempMap.get("net");
            targetCal = (long) tempMap.get("target");
            weight = (long) tempMap.get("weight");
            proteinView.setText("Protein: " + protein + " Cal");
            fatView.setText("Fat: " + fat + " Cal");
            carbsView.setText("Carbs: " + carbs + " Cal");
            netCalView.setText("Net Cal: " + netCal + " Cal");
            targetCalView.setText("Target Cal: " + targetCal + " Cal");
            weightView.setText("Weight: " + weight + " kg");
        });

    }

    private void initTextViews() {
        proteinView = findViewById(R.id.textView8);
        fatView = findViewById(R.id.textView9);
        carbsView = findViewById(R.id.textView11);
        netCalView = findViewById(R.id.textView12);
        targetCalView = findViewById(R.id.textView13);
        weightView = findViewById(R.id.textView15);
    }

    private void initImageView() {
        imageView = findViewById(R.id.imageView3);
        int rand = (new Random()).nextInt(6);
        if (rand == 0) {
            imageView.setImageResource(R.drawable.bean_stew);
        } else if (rand == 1) {
            imageView.setImageResource(R.drawable.goulash);
        } else if (rand == 2) {
            imageView.setImageResource(R.drawable.lamb_peka);
        } else if (rand == 3) {
            imageView.setImageResource(R.drawable.sandwich);
        } else if (rand == 4) {
            imageView.setImageResource(R.drawable.sardines);
        } else {
            imageView.setImageResource(R.drawable.walnutroll);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.health_record_icon);
        loadValues();
    }

    private void initWidgets() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
    }

    private void setBottomNavigationView() {
        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.health_record_icon);

        // Perform item selected listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int selectedId = item.getItemId();
            boolean isItemSelected = false;
            if(selectedId == R.id.award_icon) {
                startActivity(new Intent(getApplicationContext(), AwardActivity.class));
                overridePendingTransition(0,0);
                isItemSelected = true;
            } else if (selectedId == R.id.health_record_icon) {
                isItemSelected = true;
            } else if (selectedId == R.id.journey_icon) {
                startActivity(new Intent(getApplicationContext(), JourneyActivity.class));
                overridePendingTransition(0, 0);
                isItemSelected = true;
            }

            return isItemSelected;
        });
    }

    public void onBreakfastPressed(View view) {
        Intent intent = new Intent(this, BreakfastActivity.class);
        startActivity(intent);
    }

    public void onLunchPressed(View view) {
        Intent intent = new Intent(this, LunchActivity.class);
        startActivity(intent);
    }

    public void onDinnerPressed(View view) {
        Intent intent = new Intent(this, DinnerActivity.class);
        startActivity(intent);
    }

    public void onSnackPressed(View view) {
        Intent intent = new Intent(this, SnackActivity.class);
        startActivity(intent);
    }
}