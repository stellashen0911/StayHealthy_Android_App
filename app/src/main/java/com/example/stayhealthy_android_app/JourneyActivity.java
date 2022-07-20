package com.example.stayhealthy_android_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class JourneyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.journey_icon);

        // Perform item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.award_icon:
                        startActivity(new Intent(getApplicationContext(), AwardActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.health_record_icon:
                        startActivity(new Intent(getApplicationContext(), HealthRecordActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.journey_icon:
                        return true;
                }
                return false;
            }
        });
    }
}