package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.stayhealthy_android_app.Diet.DietActivity;
import com.example.stayhealthy_android_app.Period.PeriodActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HealthRecordActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_record);

        // Initialize and assign variable
        initWidgets();
        setBottomNavigationView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.health_record_icon);
    }

    public void openPeriodActivity(View view) {
        Intent intent = new Intent(this, PeriodActivity.class);
        startActivity(intent);
    }

    public void openWaterActivity(View view) {
        Intent intent = new Intent(this, WaterActivity.class);
        startActivity(intent);
    }

    public void openDietActivity(View view) {
        Intent intent = new Intent(this, DietActivity.class);
        startActivity(intent);
    }

    public void openWorkoutActivity(View view) {
        Intent intent = new Intent(this, WorkoutActivity.class);
        startActivity(intent);
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
}