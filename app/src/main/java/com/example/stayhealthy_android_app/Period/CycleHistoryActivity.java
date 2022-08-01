package com.example.stayhealthy_android_app.Period;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.AwardActivity;
import com.example.stayhealthy_android_app.HealthRecordActivity;
import com.example.stayhealthy_android_app.JourneyActivity;
import com.example.stayhealthy_android_app.Period.Cycle.CycleAdapter;
import com.example.stayhealthy_android_app.Period.Model.Cycle;
import com.example.stayhealthy_android_app.Period.Model.PeriodData;
import com.example.stayhealthy_android_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class CycleHistoryActivity extends AppCompatActivity {
    private List<Cycle> cycles;
    private LocalDate today;
    private DatabaseReference mDatabase;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cycle_history);

        // Get the current user from firebase authentication.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Set up the firebase Database reference.
        assert user != null;
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        // Initialize the selected date as today.
        today = LocalDate.now();

        // Initialize and assign variable
        initWidgets();

        setBottomNavigationView();

        updateCycleHistoryRV();

        syncCycleHistoryFromDB();
    }

    private void updateCycleHistoryRV() {
        RecyclerView cycleHistoryRV = findViewById(R.id.cycleHistoryRV);
        cycleHistoryRV.setLayoutManager(new LinearLayoutManager(this));
        cycleHistoryRV.setAdapter(new CycleAdapter(cycles));
    }

    private void syncCycleHistoryFromDB() {

    }

//    private void setCycleHistoryEndView() {
//        String selectedDateInStr = convertLocalDateToStringDate(today, DATE_SHORT_FORMAT);
//        DatabaseReference periodRef = mDatabase.child("period");
//        Query periodMonthQuery = periodRef.orderByChild("date").startAt(selectedDateInStr).limitToFirst(1);
//
//        periodMonthQuery.get().addOnCompleteListener(task -> {
//            if (!task.isSuccessful()) {
//                Log.v(TAG, "Error getting data", task.getException());
//            } else {
//                for (DataSnapshot ds : task.getResult().getChildren()) {
//                    PeriodData periodData = ds.getValue(PeriodData.class);
//                    if (periodData != null) {
//                        String endDate = periodData.getDate().equals(selectedDateInStr) ? selectedDateInStr : periodData.getStartDate();
//                        setCycleHistoryStartView(endDate);
//                        return;
//                    }
//                }
//                setCycleHistoryStartView(selectedDateInStr);
//            }
//        });
//    }
//
//    private void setCycleHistoryStartView(String endDate) {
//        DatabaseReference periodRef = mDatabase.child("period");
//        Query periodMonthQuery = periodRef.orderByChild("date").endBefore(endDate).limitToLast(1);
//
//        periodMonthQuery.get().addOnCompleteListener(task -> {
//            if (!task.isSuccessful()) {
//                Log.v(TAG, "Error getting data", task.getException());
//            } else {
//                String startDate = endDate;
//                for (DataSnapshot ds : task.getResult().getChildren()) {
//                    PeriodData periodData = ds.getValue(PeriodData.class);
//                    if (periodData != null) {
//                        startDate = periodData.getStartDate();
//                    }
//                }
//                setTotalCycleDaysView(startDate, endDate);
//            }
//        });
//    }
//
//    private void setTotalCycleDaysView(String startDate, String endDate) {
//        LocalDate dateBefore = LocalDate.parse(startDate);
//        LocalDate dateAfter = LocalDate.parse(endDate);
//
//        //calculating number of days in between
//        long noOfDaysBetween = ChronoUnit.DAYS.between(dateBefore, dateAfter);
//
//        if (noOfDaysBetween <= 0) {
//            periodCurrentCycleStartTV.setText(R.string.no_record_string);
//            periodCurrentCycleEndTV.setText(R.string.no_record_string);
//            periodCycleTotalDaysTV.setText(R.string.no_record_string);
//        } else {
//            String start = dateShortToLongFormat(startDate);
//            String end = dateShortToLongFormat(endDate);
//            if (start.substring(startDate.length() - 4).equals(endDate.substring(endDate.length() - 4))) {
//                start = start.substring(0, startDate.length() - 4);
//                end = endDate.substring(0, endDate.length() - 4);
//            }
//            periodCurrentCycleStartTV.setText(start);
//            periodCurrentCycleEndTV.setText(end);
//            String totalDays = noOfDaysBetween + " days";
//            periodCycleTotalDaysTV.setText(totalDays);
//        }
//    }

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
}
