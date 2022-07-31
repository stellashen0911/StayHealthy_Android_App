package com.example.stayhealthy_android_app.Diet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.stayhealthy_android_app.AwardActivity;
import com.example.stayhealthy_android_app.JourneyActivity;
import com.example.stayhealthy_android_app.R;
import com.example.stayhealthy_android_app.WaterActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DietActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet);
        initWidgets();
        setBottomNavigationView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.health_record_icon);
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