package com.example.stayhealthy_android_app.Diet;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.stayhealthy_android_app.AwardActivity;
import com.example.stayhealthy_android_app.JourneyActivity;
import com.example.stayhealthy_android_app.R;
import com.example.stayhealthy_android_app.WaterActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DietActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    private int protein;
    private int fat;
    private int carbs;
    private int netCal;
    private int targetCal;
    private int weight;
    private TextView proteinView;
    private TextView fatView;
    private TextView carbsView;
    private TextView netCalView;
    private TextView targetCalView;
    private TextView weightView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet);
        initWidgets();
        setBottomNavigationView();
        loadValues();
        initTextViews();
        fillValues();
    }

    private void loadValues() {
        protein = 1;
        fat = 2;
        carbs = 3;
        netCal = 4;
        targetCal = 5;
        weight = 63;
    }

    private void initTextViews() {
        proteinView = findViewById(R.id.textView8);
        fatView = findViewById(R.id.textView9);
        carbsView = findViewById(R.id.textView11);
        netCalView = findViewById(R.id.textView12);
        targetCalView = findViewById(R.id.textView13);
        weightView = findViewById(R.id.textView15);
    }

    @SuppressLint("SetTextI18n")
    private void fillValues() {
        proteinView.setText("Protein: " + protein + " Cal");
        fatView.setText("Fat: " + fat + " Cal");
        carbsView.setText("Carbs: " + carbs + " Cal");
        netCalView.setText("Net Cal: " + netCal + " Cal");
        targetCalView.setText("Target Cal: " + targetCal + " Cal");
        weightView.setText("Weight: " + weight + " kg");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.health_record_icon);
        loadValues();
        fillValues();
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