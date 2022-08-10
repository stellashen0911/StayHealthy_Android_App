package com.example.stayhealthy_android_app;

import static com.example.stayhealthy_android_app.Water.WaterIntakeModel.DAILY_WATER_TARGET_OZ;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stayhealthy_android_app.Award.AwardAdapter;
import com.example.stayhealthy_android_app.Award.Model.AwardData;
import com.example.stayhealthy_android_app.Award.Model.AwardDisplay;
import com.example.stayhealthy_android_app.Water.WaterIntakeModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AwardActivity<x> extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "MAwardActivity";
    private final static String DATE_SHORT_FORMAT = "yyyy-MM-dd";
    private static final String WATER_INTAKE_DB_NAME = "water_intake";
    private static final String DIET_DB_NAME = "diets";
    private static final String WORKOUT_DB_NAME = "work-out";
    private static final String AWARD_DB_NAME = "award";
    private static final List<String> AWARD_NAME = new ArrayList<>(Arrays.asList("Water Drink Goal 100%", "Diet Goal 100%", "Workout Goal 100%"));
    private static final List<Integer> TARGET = new ArrayList<>(Arrays.asList(3, 7, 30, 100, 365, 1000));
    private DatabaseReference mDatabase;
    private String today;
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView profile_nv;
    private RecyclerView receivedAwardRV;
    private RecyclerView notReceivedAwardRV;
    private List<AwardDisplay> receivedAwardDisplayList;
    private List<AwardDisplay> notReceivedAwardDisplayList;

    //new to add
    static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int CAMERA_REQUEST = 1888;
    FirebaseStorage fStorage;
    FirebaseUser user;
    StorageReference storageReference;
    ImageView user_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_award);

        // Get Today in string.
        today = localDateToDateInStr(LocalDate.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL dd yyyy");
        String todayForWorkout = LocalDate.now().format(formatter);

        // Get the current user from firebase authentication.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Set up the firebase Database reference.
        assert user != null;
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        // Register to listen to the data change in water_intake attribute.
        DatabaseReference waterRef = mDatabase.child(WATER_INTAKE_DB_NAME).child(today);
        waterRef.addValueEventListener(waterListener);

        DatabaseReference dietRef = mDatabase.child(DIET_DB_NAME).child(today);
        dietRef.addValueEventListener(dietListener);

        DatabaseReference workoutRef = mDatabase.child(WORKOUT_DB_NAME).child(todayForWorkout);
        workoutRef.addValueEventListener(workoutListener);

        // Initialize and assign variable
        initWidgets();
        setBottomNavigationView();
        initProfileDrawer();

        notReceivedAwardDisplayList = new ArrayList<>();
        receivedAwardDisplayList = new ArrayList<>();

        setNotReceivedAwardRecyclerView();

        setReceivedAwardRecycleView();

    }

    ValueEventListener waterListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            WaterIntakeModel value = snapshot.getValue(WaterIntakeModel.class);
            if (value != null) {
                if (value.getWaterOz() >= DAILY_WATER_TARGET_OZ) {
                    AwardData newData = new AwardData(today, AWARD_NAME.get(0), 1);
                    syncAwardDataWithDatabase(newData);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.w(TAG, "Load water database cancelled", error.toException());
        }
    };

    ValueEventListener dietListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            HashMap tempMap = (HashMap) snapshot.getValue();
            try {
                long breakfastNet = (long) ((HashMap) tempMap.get("breakfast")).get("net");
                long lunchNet = (long) ((HashMap) tempMap.get("lunch")).get("net");
                long dinnerNet = (long) ((HashMap) tempMap.get("dinner")).get("net");
                long snackNet = (long) ((HashMap) tempMap.get("snack")).get("net");
                long netCal = breakfastNet + lunchNet + dinnerNet + snackNet;
                long targetCal = (long) tempMap.get("target");
                if (netCal <= targetCal) {
                    AwardData newData = new AwardData(today, AWARD_NAME.get(1), 1);
                    syncAwardDataWithDatabase(newData);
                }
            } catch (Exception err) {
                Log.w(TAG, "Load diet database exception", err);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.w(TAG, "Load diet database cancelled", error.toException());
        }
    };

    ValueEventListener workoutListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            HashMap tempMap = (HashMap) snapshot.getValue();
            try {
                boolean finished = (boolean)(tempMap.get("today_goal_finished_status"));
                if (finished) {
                    AwardData newData = new AwardData(today, AWARD_NAME.get(2), 1);
                    syncAwardDataWithDatabase(newData);
                }
            } catch (Exception err) {
                Log.w(TAG, "Load workout database exception", err);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.w(TAG, "Load workout database cancelled", error.toException());
        }
    };

    // Write the new award data to Database.
    private void saveAwardDataToDatabase(AwardData awardData) {
        DatabaseReference ref = mDatabase.child(AWARD_DB_NAME).child(awardData.getName());
        ref.setValue(awardData)
                .addOnSuccessListener(unused -> Log.v(TAG, "write one award data to database is successful"))
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // Save the received times of user's award to database.
    private void syncAwardDataWithDatabase(AwardData newData) {
        DatabaseReference ref = mDatabase.child(AWARD_DB_NAME).child(newData.getName());

        ref.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting award data from firebase Database", task.getException());
            } else {
                AwardData awardData = task.getResult().getValue(AwardData.class);
                if (awardData == null) {
                    saveAwardDataToDatabase(newData);
                } else if (!awardData.getDate().equals(newData.getDate())) {
                    // If today's new award has not been saved to database, update it.
                    awardData.addTimes(); // Increment the received times.
                    awardData.setDate(newData.getDate()); // Update the date to the new date.
                    saveAwardDataToDatabase(awardData);
                }
            }
        });
    }

    // Binary Search used to find the insert position of one number in an ascending array.
    private int getPosition(int num) {
        int left = 0;
        int right = TARGET.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (num == TARGET.get(mid)) {
                return mid;
            } else if (num < TARGET.get(mid)) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return left;
    }

    // Generate received/not received Award Display based on the dataSnapshot queried from the database.
    // There are two kinds of awards. One is the daily goal award and another is the long-term goal
    // award which rewards users if they meet the daily goal for `TARGET` days.
    private void generateAwardDisplayAndAddToDisplayList(DataSnapshot dataSnapshot, String namePrefix) {
        AwardDisplay awardDisplayToday = new AwardDisplay(namePrefix, "Today");
        // If there is no loaded data, means both awards are not received, add both to notReceivedList.
        if (dataSnapshot.getValue() == null) {
            notReceivedAwardDisplayList.add(0, awardDisplayToday);
            notReceivedAwardDisplayList.add(new AwardDisplay(namePrefix + " | 3 Days", "0/3 Days"));
        }
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            AwardData awardData = ds.getValue(AwardData.class);
            if (awardData == null) {
                return;
            }

            // First determine today's goal, if the award data's date is today, means this data
            // gets updated today, means the user achieves this goal today.
            if (awardData.getDate().equals(today)) {
                receivedAwardDisplayList.add(0, awardDisplayToday);
            } else {
                notReceivedAwardDisplayList.add(0, awardDisplayToday);
            }

            // Second determine the long term award.
            int times = awardData.getTimes(); // the received times of this award.
            // Find the insert position of `times` in the TARGET array. The `num` smaller than `times`
            // is the user achieved award. The `num` larger than `times` is the user not received award.
            int position = getPosition(times);
            String numOfDays; // number of days in the long-term award details.
            // If the times meet one of the number in `TARGET`, means user achieve that `num` goal medal.
            if (TARGET.contains(times)) {
                numOfDays = TARGET.get(position) + " Days";
                receivedAwardDisplayList.add(new AwardDisplay(namePrefix, numOfDays));
                // If user has achieved goal for 1000 days, there is no more long term award.
                if (position != TARGET.size() - 1) {
                    numOfDays = TARGET.get(position + 1) + " Days";
                    notReceivedAwardDisplayList.add(new AwardDisplay(namePrefix + " | " + numOfDays, times + "/" + numOfDays));
                }
            } else {
                // If `times` is smaller than 3, there's no received long term award.
                if (position != 0) {
                    numOfDays = TARGET.get(position - 1) + " Days";
                    receivedAwardDisplayList.add(new AwardDisplay(namePrefix , numOfDays));
                }
                // If `times` is larger than 1000, there's no not received long-term award.
                if (position != TARGET.size()) {
                    numOfDays = TARGET.get(position) + " Days";
                    notReceivedAwardDisplayList.add(new AwardDisplay(namePrefix + " | " + numOfDays, times + "/" + numOfDays));
                }
            }
        }
    }

    // Read award data from database and update UI.
    private void getAwardDataFromDatabaseAndUpdateRecyclerView() {
        notReceivedAwardDisplayList = new ArrayList<>();
        receivedAwardDisplayList = new ArrayList<>();

        DatabaseReference awardRef = mDatabase.child(AWARD_DB_NAME);
        List<Task<DataSnapshot>> tasks = new ArrayList<>(); // multiple tasks list
        // Create tasks for query in Award database, ordered by award name.
        for (String name : AWARD_NAME) {
            Query query = awardRef.orderByChild("name").equalTo(name);
            tasks.add(query.get());
        }

        Tasks.whenAllSuccess(tasks)
                .addOnFailureListener(Throwable::printStackTrace)
                .addOnSuccessListener(list -> {
                    // Loop over the list of task result.
                    for(int i = 0; i < AWARD_NAME.size(); i ++) {
                        DataSnapshot dataSnapshot = (DataSnapshot) list.get(i);
                        String namePrefix = AWARD_NAME.get(i);
                        generateAwardDisplayAndAddToDisplayList(dataSnapshot, namePrefix);
                    }
                    setReceivedAwardRecyclerViewAdapter();
                    setNotReceivedAwardRecyclerViewAdapter();
                });
    }

    private void setNotReceivedAwardRecyclerViewAdapter() {
        int itemLayout = R.layout.item_award_card_list;
        notReceivedAwardRV.setAdapter(new AwardAdapter(notReceivedAwardDisplayList, itemLayout));
    }

    private void setNotReceivedAwardRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        notReceivedAwardRV.setLayoutManager(linearLayoutManager);
        setNotReceivedAwardRecyclerViewAdapter();
    }

    private void setReceivedAwardRecyclerViewAdapter() {
        int itemLayout = R.layout.item_award_card_grid;
        receivedAwardRV.setAdapter(new AwardAdapter(receivedAwardDisplayList, itemLayout));
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
        TextView userNameText = (TextView) headerView.findViewById(R.id.user_name_show);
        user_image = (ImageView) headerView.findViewById(R.id.image_avatar);
        fStorage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = fStorage.getReference("users").child(user.getUid());

        // calling add value event listener method
        // for getting the values from database.
        DatabaseReference email_ref = mDatabase.child("email");

        email_ref.get().addOnCompleteListener(task -> {
            try {
                String email = (String) task.getResult().getValue();
                userNameText.setText(email);
            } catch (Exception err) {
                System.out.println("error retreive data from database");
            }
        });

        LogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        ChangeAvartaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddPicturePressed(v);
            }
        });
    }
    //new to add
    public void onAddPicturePressed(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // simply return to the last activity.
            onBackPressed();
        }
    }

    //new to add
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            user_image.setImageBitmap(photo);
        }
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

        getAwardDataFromDatabaseAndUpdateRecyclerView();
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
    private String localDateToDateInStr(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_SHORT_FORMAT);
        return date.format(dateTimeFormatter);
    }

    // Convert month value to short month string.
    private String monthValueToMonthShort(int month) {
        return new DateFormatSymbols().getShortMonths()[month - 1];
    }
}