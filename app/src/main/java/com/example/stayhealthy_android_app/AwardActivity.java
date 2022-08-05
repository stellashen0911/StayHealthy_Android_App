package com.example.stayhealthy_android_app;

import static com.example.stayhealthy_android_app.Water.WaterIntakeModel.DAILY_WATER_TARGET_OZ;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.stayhealthy_android_app.Award.AwardAdapter;
import com.example.stayhealthy_android_app.Award.Model.AwardData;
import com.example.stayhealthy_android_app.Water.WaterIntakeModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AwardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "MAwardActivity";
    private final static String DATE_SHORT_FORMAT = "yyyy-MM-dd";
    private final static String DATE_LONG_FORMAT = "MMM dd yyyy";
    private static final String WATER_INTAKE_DB_NAME = "water_intake";
    private static final String AWARD_DB_NAME = "award";
    private static final List<String> AWARD_NAME = new ArrayList<>(Arrays.asList("Water Drink Goal 100%", "Diet Goal 100%", "Workout Goal 100%"));
    private static final List<Integer> TARGET = new ArrayList<>(Arrays.asList(3, 7, 100, 365));
    private DatabaseReference mDatabase;
    private String today;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView profile_nv;
    private RecyclerView receivedAwardRV;
    private RecyclerView notReceivedAwardRV;
    private List<AwardData> receivedAwardDataList;
    private List<AwardData> notReceivedAwardDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_award);

        // Get the current user from firebase authentication.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Set up the firebase Database reference.
        assert user != null;
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        // Get Today in string.
        today = localDateToDateInStr(LocalDate.now(), DATE_SHORT_FORMAT);

        // Initialize and assign variable
        initWidgets();
        setBottomNavigationView();
        initProfileDrawer();

        notReceivedAwardDataList = new ArrayList<>();
        receivedAwardDataList = new ArrayList<>();

        setNotReceivedAwardRecyclerView();

        setReceivedAwardRecycleView();

    }

//    private void syncWithDatabase(AwardData awardData, AwardLabel label) {
//        DatabaseReference ref = mDatabase.child(AWARD_DB_NAME).child(label.toString());
//        ref.setValue(awardData)
//                .addOnSuccessListener(unused -> {
//                    Log.v(TAG, "write one award data to database is successful");
//                    checkLongTermGoal(label);
//                })
//                .addOnFailureListener(Throwable::printStackTrace);
//    }
//
//    private void saveReceivedAwardDataToDatabase(AwardData awardData) {
//        AwardLabel awardLabel = AwardLabel.valueOfName(awardData.getName());
//        if (awardLabel == null) {
//            return;
//        }
//        DatabaseReference ref = mDatabase.child(AWARD_DB_NAME).child(awardLabel.toString());
//
//        ref.get().addOnCompleteListener(task -> {
//            if (!task.isSuccessful()) {
//                Log.e(TAG, "Error getting award data from firebase Database", task.getException());
//            } else {
//                AwardData data = task.getResult().getValue(AwardData.class);
//                if (data != null && !data.getDate().equals(awardData.getDate())) {
//                    data.addTimes();
//                    data.setDate(awardData.getDate());
//                    syncWithDatabase(data, awardLabel);
//                } else {
//                    awardData.setType(0);
//                    syncWithDatabase(awardData, awardLabel);
//                }
//            }
//        });
//    }
//
//    private void checkTodayWaterDrinkGoal() {
//        // Query today's water intake data.
//        DatabaseReference waterIntakeRef = mDatabase.child(WATER_INTAKE_DB_NAME);
//        Query query = waterIntakeRef.orderByChild("date").equalTo(today);
//
//        query.get().addOnCompleteListener(task -> {
//            if (!task.isSuccessful()) {
//                Log.e(TAG, "Error getting water intake data from firebase Database", task.getException());
//            } else {
//                for (DataSnapshot ds : task.getResult().getChildren()) {
//                    WaterIntakeModel value = ds.getValue(WaterIntakeModel.class);
//                    if (value != null) {
//                        if (value.getWaterOz() >= DAILY_WATER_TARGET_OZ) {
//                            AwardData awardData = new AwardData(today, AwardLabel.Water.name, 1, "Today", 1);
//                            addDataToReceivedAwardDataList(0, awardData);
//                            saveReceivedAwardDataToDatabase(awardData);
//                            return;
//                        }
//                    }
//                }
//                Log.v(TAG, "reach here");
//                AwardData awardData = new AwardData(today, AwardLabel.Water.name, 0, "Today", 1);
//                addDataToNotReceivedAwardDataList(awardData);
//            }
//        });
//    }
//
//    private int getPosition(int num) {
//        int left = 0;
//        int right = TARGET.size() - 1;
//
//        while (left <= right) {
//            int mid = left + (right - left) / 2;
//            if (num == TARGET.get(mid)) {
//                return mid;
//            } else if (num < TARGET.get(mid)) {
//                right = mid - 1;
//            } else {
//                left = mid + 1;
//            }
//        }
//
//        return left;
//    }
//
//    private void addDataToReceivedAwardDataList(int position, AwardData awardData) {
//        receivedAwardDataList.add(position, awardData);
//        Objects.requireNonNull(receivedAwardRV.getAdapter()).notifyItemInserted(position);
//    }
//
//    private void addDataToNotReceivedAwardDataList(AwardData awardData) {
//        if(notReceivedAwardDataList.contains(awardData)) {
//            int position = notReceivedAwardDataList.indexOf(awardData);
//            notReceivedAwardDataList.remove(awardData);
//            Objects.requireNonNull(notReceivedAwardRV.getAdapter()).notifyItemRemoved(position);
//        }
//        notReceivedAwardDataList.add(awardData);
//        Objects.requireNonNull(notReceivedAwardRV.getAdapter()).notifyItemInserted(notReceivedAwardDataList.size());
//    }
//
//    private void checkLongTermGoal(AwardLabel label) {
//        DatabaseReference ref = mDatabase.child(AWARD_DB_NAME).child(label.toString());
//
//        ref.get().addOnCompleteListener(task -> {
//            if (!task.isSuccessful()) {
//                Log.e(TAG, "Error getting water intake data from firebase Database", task.getException());
//            } else {
//                AwardData awardData = task.getResult().getValue(AwardData.class);
//                if (awardData != null) {
//                    int times = awardData.getTimes();
//                    int position = getPosition(times);
//                    if (TARGET.contains(times)) {
//                        awardData.setDetails(times + " days");
//                        addDataToReceivedAwardDataList(receivedAwardDataList.size(), awardData);
//                        if (times == TARGET.get(TARGET.size() - 1)) {
//                            return;
//                        }
//                        awardData.setDetails(times + "/" + TARGET.get(position + 1) + " days");
//                        addDataToNotReceivedAwardDataList(awardData);
//                    } else {
//                        if (position != 0) {
//                            awardData.setDetails(TARGET.get(position - 1) + " days");
//                            addDataToReceivedAwardDataList(receivedAwardDataList.size(), awardData);
//                        }
//                        awardData.setDetails(times + "/" + TARGET.get(position) + " days");
//                        addDataToNotReceivedAwardDataList(awardData);
//                    }
//                } else {
//                    awardData = new AwardData(today, label.name, 0, "0/3 days", 0);
//                    addDataToNotReceivedAwardDataList(awardData);
//                }
//            }
//        });
//    }
//
//    private void checkGoalAndUpdateRecyclerView() {
//        checkTodayWaterDrinkGoal();
//        for(AwardLabel awardLabel : AwardLabel.values()) {
//            checkLongTermGoal(awardLabel);
//        }
//    }

    private void setNotReceivedAwardRecyclerViewAdapter() {
        int itemLayout = R.layout.item_award_card_list;
        notReceivedAwardRV.setAdapter(new AwardAdapter(notReceivedAwardDataList, itemLayout));
    }

    private void setNotReceivedAwardRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        notReceivedAwardRV.setLayoutManager(linearLayoutManager);
        setNotReceivedAwardRecyclerViewAdapter();
    }

    private void setReceivedAwardRecyclerViewAdapter() {
        int itemLayout = R.layout.item_award_card_grid;
        receivedAwardRV.setAdapter(new AwardAdapter(receivedAwardDataList, itemLayout));
    }

    private void setReceivedAwardRecycleView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        receivedAwardRV.setLayoutManager(gridLayoutManager);
        setReceivedAwardRecyclerViewAdapter();
    }

    private void initProfileDrawer() {
        // Initialize profile drawer
        drawer = findViewById(R.id.drawer_layout);
        profile_nv = findViewById(R.id.nav_view_award);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Award");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        profile_nv.setNavigationItemSelectedListener(this);

        //set up the header button listeners
        View headerView = profile_nv.getHeaderView(0);
        Button LogOutBtn = (Button) headerView.findViewById(R.id.profile_logout_btn);
        Button ChangeAvartaButton = (Button) headerView.findViewById(R.id.update_profile_image_btn);

        LogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        ChangeAvartaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to do:
            }
        });
    }

    private void initWidgets() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        receivedAwardRV = findViewById(R.id.receivedAwardRV);
        notReceivedAwardRV = findViewById(R.id.notReceivedAwardRV);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.award_icon);

//        checkGoalAndUpdateRecyclerView();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setBottomNavigationView() {
        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.award_icon);

        // Perform item selected listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int selectedId = item.getItemId();
            boolean isItemSelected = false;
            if(selectedId == R.id.award_icon) {
                isItemSelected = true;
            } else if (selectedId == R.id.health_record_icon) {
                startActivity(new Intent(getApplicationContext(), HealthRecordActivity.class));
                overridePendingTransition(0,0);
                isItemSelected = true;
            } else if (selectedId == R.id.journey_icon) {
                startActivity(new Intent(getApplicationContext(), JourneyActivity.class));
                overridePendingTransition(0, 0);
                isItemSelected = true;
            }

            return isItemSelected;
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        switch(item.getItemId()) {
            case R.id.nav_settings:
                drawer.closeDrawers();
                Intent i = new Intent(AwardActivity.this, SettingsActivity.class);
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

    // Expand and collapse ReceivedAward Recycler View
    public void expandAndCollapseReceivedAward(View view) {
        MaterialButton receivedAwardBTN = findViewById(R.id.receivedAwardExpandBTN);
        if (receivedAwardRV.getVisibility() == View.GONE) {
            TransitionManager.beginDelayedTransition(receivedAwardRV, new AutoTransition());
            receivedAwardRV.setVisibility(View.VISIBLE);
            receivedAwardBTN.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_expand_less_24));
        } else {
            TransitionManager.beginDelayedTransition(receivedAwardRV, new AutoTransition());
            receivedAwardRV.setVisibility(View.GONE);
            receivedAwardBTN.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_expand_more_24));
        }
    }

    // Expand and collapse NotReceivedAward Recycler View
    public void expandAndCollapseNotReceivedAward(View view) {
        MaterialButton notReceivedAwardBTN = findViewById(R.id.notReceivedAwardExpandBTN);
        if (notReceivedAwardRV.getVisibility() == View.GONE) {
            TransitionManager.beginDelayedTransition(notReceivedAwardRV, new AutoTransition());
            notReceivedAwardRV.setVisibility(View.VISIBLE);
            notReceivedAwardBTN.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_expand_less_24));
        } else {
            TransitionManager.beginDelayedTransition(notReceivedAwardRV, new AutoTransition());
            notReceivedAwardRV.setVisibility(View.GONE);
            notReceivedAwardBTN.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_expand_more_24));
        }
    }

    // Convert LocalDate to date in specified string format.
    private String localDateToDateInStr(LocalDate date, String dateFormat) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
        return date.format(dateTimeFormatter);
    }
}