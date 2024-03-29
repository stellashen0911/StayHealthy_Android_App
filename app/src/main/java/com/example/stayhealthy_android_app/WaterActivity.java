package com.example.stayhealthy_android_app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.Water.WaterIntakeModel;
import com.example.stayhealthy_android_app.Water.WaterIntakeAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class WaterActivity  extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    RecyclerView waterListRecyclerView;
    ImageButton addGlassWaterButton;
    ImageButton addBottleWaterButton;
    ImageButton addLargeBottleWaterButton;
    private List<WaterIntakeModel> waterIntakesList;
    private DatabaseReference myDataBase;
    private static final String WATER_INTAKE_DB_NAME = "water_intake";
    WaterIntakeAdapter waterIntakeAdapter;
    private static final String TODAY_WATER_OZ_CONST = " oz of your 64 oz goal";
    TextView todayWaterTextView;
    TextView dailyPercentageTextView;
    ProgressBar dailyProgressBar;
    Handler handler;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water);
        handler = new Handler();
        dailyPercentageTextView = findViewById(R.id.percentage);
        dailyProgressBar = findViewById(R.id.daily_progress_bar);
        dailyProgressBar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
        todayWaterTextView = findViewById(R.id.today_status);
        waterIntakesList = new ArrayList<>();
        waterListRecyclerView = findViewById(R.id.water_intake_recycler_view);
        waterListRecyclerView.setHasFixedSize(false);
        waterIntakeAdapter = new WaterIntakeAdapter(waterIntakesList,this);
        waterListRecyclerView.setAdapter(waterIntakeAdapter);
        waterListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addGlassWaterButton = findViewById(R.id.glass_water);
        addBottleWaterButton = findViewById(R.id.bottle_water);
        addLargeBottleWaterButton = findViewById(R.id.large_bottle_water);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        myDataBase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        readWaterData (30,  waterIntakeAdapter );
        // Initialize and assign variable
        addGlassWaterButton.setOnClickListener((v)->addWaterIntake(8));
        addBottleWaterButton.setOnClickListener((v)->addWaterIntake(16));
        addLargeBottleWaterButton.setOnClickListener((v)->addWaterIntake(24));
        initWidgets();
        setBottomNavigationView();

        //set up the toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Water Records");
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

    public void readWaterData (int numDays, WaterIntakeAdapter waterIntakeAdapter ) {
        DatabaseReference waterDbRef = myDataBase.child(WATER_INTAKE_DB_NAME);

        Query waterIntakeQueryLastMonth = waterDbRef.orderByChild("date").limitToLast(numDays);
        waterIntakeQueryLastMonth.get().addOnCompleteListener((task -> {
            HashMap<String, HashMap> tempMap = (HashMap) task.getResult().getValue();
            if(tempMap == null) {
                tempMap = new HashMap<>();
            }
            List<String> dates = new ArrayList<>(tempMap.keySet());
            dates.sort(Comparator.reverseOrder());
            Date currentDate = Calendar.getInstance().getTime();
            String currentDateInStr = convertDateToDateStr(currentDate);
            int startIndex = 0;
            if (dates.size() < 1 ||  !dates.get(0).equals(currentDateInStr)) {
                startIndex = 1;
                WaterIntakeModel todayModel = new WaterIntakeModel(0, currentDateInStr);
                waterIntakesList.add(todayModel);
                waterDbRef.child(currentDateInStr).setValue(todayModel);
            }
            for (int i = startIndex; i < dates.size(); i++) {
                Long waterOz =(long)tempMap.get(dates.get(i)).get("waterOz");
                String date  = (String)tempMap.get(dates.get(i)).get("date");
                waterIntakesList.add(new WaterIntakeModel(waterOz,date));
                waterIntakeAdapter.notifyDataSetChanged();
            }
            todayWaterTextView.setText(waterIntakesList.get(0).getWaterOz() + TODAY_WATER_OZ_CONST );
            Long percentage = (waterIntakesList.get(0).getWaterOz()*100) / WaterIntakeModel.DAILY_WATER_TARGET_OZ;
            final Long finalPercentage = percentage > 100 ? 100 :percentage;
            dailyPercentageTextView.setText(finalPercentage+"%");
            handler.post(() -> dailyProgressBar.setProgress(finalPercentage.intValue()));

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
    private void addWaterIntake(long waterOz){
        DatabaseReference waterDbRef = myDataBase.child(WATER_INTAKE_DB_NAME);
        WaterIntakeModel todayModel = null;
        Date currentDate = Calendar.getInstance().getTime();
        String dateStr = convertDateToDateStr(currentDate);
        for(int i =0;i<waterIntakesList.size();i++) {
            if (dateStr.equals(waterIntakesList.get(i).getDate())) {
                todayModel = waterIntakesList.get(i);
                break;
            }
        }
        if (todayModel == null) {
            todayModel = new WaterIntakeModel(0,dateStr);
        }
        todayModel.addWater(waterOz);
        waterDbRef.child(dateStr).setValue(todayModel);
        todayWaterTextView.setText(todayModel.getWaterOz() + TODAY_WATER_OZ_CONST);
        Long percentage = (todayModel.getWaterOz() * 100) / WaterIntakeModel.DAILY_WATER_TARGET_OZ;
        final Long finalPercentage = percentage > 100 ? 100 : percentage;
        dailyPercentageTextView.setText(finalPercentage + "%");
        handler.post(() -> dailyProgressBar.setProgress(finalPercentage.intValue()));
        waterIntakeAdapter.notifyDataSetChanged();
    }

    // Convert date to date in string
    private String convertDateToDateStr(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(/*dateFormat=*/"yyyy-MM-dd", Locale.US);
        return format.format(date);
    }
}
