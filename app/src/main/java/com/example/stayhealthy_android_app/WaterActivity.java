package com.example.stayhealthy_android_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.Journey.JourneyPost;
import com.example.stayhealthy_android_app.Water.WaterIntakeModel;
import com.example.stayhealthy_android_app.Water.WaterIntakeAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class WaterActivity  extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    RecyclerView waterListRecyclerView;
    ImageButton addGlassWaterButton;
    ImageButton addBottleWaterButton;
    ImageButton addLargeBottleWaterButton;
    private List<WaterIntakeModel> waterIntakesList;
    private DatabaseReference myDataBase;
    private static final String WATER_INTAKE_DB_NAME = "water_intake";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water);
        waterIntakesList = new ArrayList<>();
        waterListRecyclerView = findViewById(R.id.water_intake_recycler_view);
        waterListRecyclerView.setHasFixedSize(false);
        WaterIntakeAdapter waterIntakeAdapter = new WaterIntakeAdapter(waterIntakesList,this);
        waterListRecyclerView.setAdapter(waterIntakeAdapter);
        waterListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        addGlassWaterButton = findViewById(R.id.glass_water);
        addBottleWaterButton = findViewById(R.id.bottle_water);
        addLargeBottleWaterButton = findViewById(R.id.large_bottle_water);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myDataBase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        produceFakeData ();
        readWaterData (30,  waterIntakeAdapter );
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

    private void initWidgets() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
    }

    public void onWaterButtonPressed(int waterOZ) {
        // submit water oz to database.
    }

    public void readWaterData (int numDays, WaterIntakeAdapter waterIntakeAdapter ) {
        DatabaseReference waterDbRef = myDataBase.child(WATER_INTAKE_DB_NAME);
        Query waterIntakeQueryLastMonth = waterDbRef.orderByChild("date").limitToFirst(numDays);
        waterIntakeQueryLastMonth.get().addOnCompleteListener((task -> {
            HashMap<String, HashMap> tempMap = (HashMap) task.getResult().getValue();
            List<String> dates = new ArrayList<>(tempMap.keySet());
            dates.sort(Comparator.naturalOrder());
            for (int i = 0; i < dates.size(); i++) {
                Long waterOz =(long)tempMap.get(dates.get(i)).get("waterOz");
                String date  = (String)tempMap.get(dates.get(i)).get("date");
                waterIntakesList.add(new WaterIntakeModel(waterOz,date));
                waterIntakeAdapter.notifyDataSetChanged();
            }
        }));
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

    private void produceFakeData () {
        DatabaseReference waterDbRef = myDataBase.child(WATER_INTAKE_DB_NAME);
        waterDbRef.removeValue();
        for (int i = 1 ; i<=9; i++) {
            String dateStr = "2022-01-0"+i;
            waterDbRef.child(dateStr).setValue(new WaterIntakeModel(40,dateStr));
        }
        for (int i = 10 ; i<=31; i++) {
            String dateStr = "2022-01-"+i;
            long waterOz = 40;
            if( i % 7  == 0 ) {
                waterOz = WaterIntakeModel.DAILY_WATER_TARGET_OZ;
            }
            waterDbRef.child(dateStr).setValue(new WaterIntakeModel(waterOz,dateStr));
        }
    }
}
