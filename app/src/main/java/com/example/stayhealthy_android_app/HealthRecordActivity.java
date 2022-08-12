package com.example.stayhealthy_android_app;

import static com.example.stayhealthy_android_app.EditPostActivity.RotateBitmap;
import static com.example.stayhealthy_android_app.Period.PeriodActivity.MONTHLY_PERIOD;
import static com.example.stayhealthy_android_app.Water.WaterIntakeModel.DAILY_WATER_TARGET_OZ;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.stayhealthy_android_app.Award.Model.AwardData;
import com.example.stayhealthy_android_app.Diet.DietActivity;
import com.example.stayhealthy_android_app.Period.Model.PeriodData;
import com.example.stayhealthy_android_app.Period.PeriodActivity;
import com.example.stayhealthy_android_app.Water.WaterIntakeModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.HashMap;

public class HealthRecordActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "MHealthRecordActivity";
    private final static String DATE_FULL_FORMAT = "EEEE, MMMM d, yyyy";
    private final static String DATE_SHORT_FORMAT = "yyyy-MM-dd";
    private final static String DATE_LONG_FORMAT = "MMM dd";
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView profile_nv;
    private DatabaseReference mDatabase;
    private DatabaseReference staticDietDB;
    private DatabaseReference dieDB;
    private DatabaseReference waterDB;
    private DatabaseReference periodDB;
    private DatabaseReference workoutDB;

    private ProgressBar pbDiet;
    private ProgressBar pbWater;
    private ProgressBar pbPeriod;
    private ProgressBar pbWorkout;

    //new to add
    static final int REQUEST_IMAGE_CAPTURE = 100;
    FirebaseUser user;
    ImageView user_image;
    // Uri indicates, where the image will be picked from
    private Uri filePath;
    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storage_reference;
    byte [] currentImageBytes;
    final long FIVE_MEGABYTE = 1024 * 1024 * 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_record);

        initProgressBars();
        staticDietDB = FirebaseDatabase.getInstance().getReference("user").
                child("test@gmail_com").child("diets").child("20220731");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        mDatabase.child("notification_settings").child("Work-Out Notification").setValue(true);
        mDatabase.child("notification_settings").child("Water Notification").setValue(true);
        mDatabase.child("notification_settings").child("Diet Notification").setValue(true);
        mDatabase.child("notification_settings").child("Period Notification").setValue(true);


        dieDB = mDatabase.child("diets").child(java.time.LocalDate.now().toString());
        waterDB = mDatabase.child("water_intake").child(java.time.LocalDate.now().toString());
        periodDB = mDatabase.child("period");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL dd yyyy");
        workoutDB = mDatabase.child("work-out").child(LocalDate.now().format(formatter));

        initWidgets();
        setBottomNavigationView();
        initProfileDrawer();

        // Set date and welcome user text
        setDateAndWelcomeUserTextView();

        //set up the notification
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 10);
//        calendar.set(Calendar.MINUTE, 30);
//        calendar.set(Calendar.SECOND, 0);
//        System.out.println("here 1");
//        Intent intent1 = new Intent(HealthRecordActivity.this, AlarmReceiver.class);
//        System.out.println("here 2");
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(HealthRecordActivity.this, 0 ,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//        System.out.println("here 3");
//        AlarmManager alarmManager = (AlarmManager) HealthRecordActivity.this.getSystemService(HealthRecordActivity.this.ALARM_SERVICE);
//        System.out.println("here 4");
//        if (alarmManager != null) {
//            System.out.println("here in side if statement");
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
//        }
//        System.out.println("here 5");
    }

    private void updatePBDiet() {
        staticDietDB.get().addOnCompleteListener(task -> {
            TextView dietProgressBarTV = findViewById(R.id.dietProgressBarTV);
            TextView dietDetailsTV = findViewById(R.id.dietDetailsTV);
            try {
                HashMap tempMap = (HashMap) task.getResult().getValue();
                long breakfastNet = (long) ((HashMap) tempMap.get("breakfast")).get("net");
                long lunchNet = (long) ((HashMap) tempMap.get("lunch")).get("net");
                long dinnerNet = (long) ((HashMap) tempMap.get("dinner")).get("net");
                long snackNet = (long) ((HashMap) tempMap.get("snack")).get("net");
                long netCal = breakfastNet + lunchNet + dinnerNet + snackNet;
                long targetCal = (long) tempMap.get("target");
                double v = 100 * (double) netCal / targetCal;
                if (v == 0) {
                    return;
                }
                pbDiet.setMin(0);
                pbDiet.setProgress((int) v);
                String percentInStr = ((int) v) + "%";
                dietProgressBarTV.setText(percentInStr);
                String goalStr = "Goal " + ((int) targetCal) + " Cal";
                dietDetailsTV.setText(goalStr);
                dieDB.child("breakfast").child("net").setValue(breakfastNet);
                dieDB.child("lunch").child("net").setValue(lunchNet);
                dieDB.child("dinner").child("net").setValue(dinnerNet);
                dieDB.child("snack").child("net").setValue(snackNet);
                dieDB.child("target").setValue(targetCal);
            } catch (Exception err) {
                dietProgressBarTV.setText(R.string._0_percent_string);
                dietDetailsTV.setText(R.string.goal_string);
            }
        });
    }

    private void updatePBWater() {
        waterDB.get().addOnCompleteListener(task -> {
            TextView waterProgressBarTV = findViewById(R.id.waterProgressBarTV);
            WaterIntakeModel value = task.getResult().getValue(WaterIntakeModel.class);
            if (value != null && value.getWaterOz() != 0) {
                long waterOz = value.getWaterOz();
                long percentage = 100 * waterOz / DAILY_WATER_TARGET_OZ;
                final long finalPercentage = percentage > 100 ? 100 :percentage;
                pbWater.setMin(0);
                // Set water progress
                pbWater.setProgress((int) finalPercentage);
                Log.v(TAG, "not null" + pbWater.getProgress());
                // Set water progress text
                String percentInStr = (int) finalPercentage + "%";
                waterProgressBarTV.setText(percentInStr);
            }
        });
    }

    private void updatePBPeriod() {
        LocalDate today = LocalDate.now();
        String date = localDateToDateInStr(today, DATE_SHORT_FORMAT);
        Query query = periodDB.orderByChild("flowAndDate").endAt("1-" + date).limitToLast(1);

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v("", "Error getting data", task.getException());
            } else {
                TextView periodProgressBarTV = findViewById(R.id.periodProgressBarTV);
                TextView periodDetailsTV = findViewById(R.id.periodDetailsTV);
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null && periodData.getHadFlow()) {
                        LocalDate startDate = LocalDate.parse(periodData.getStartDate());
                        int times = (int) (calculateDaysBetween(periodData.getStartDate(), date) / MONTHLY_PERIOD + 1);
                        // Calculated PredictedDate in the format "MMM dd yyyy"
                        LocalDate predictedDate = startDate.plusDays(MONTHLY_PERIOD * times);
                        String predictedDateInStr = localDateToDateInStr(predictedDate, DATE_SHORT_FORMAT);
                        int remainingDays = (int) calculateDaysBetween(date, predictedDateInStr);
                        // Set progress bar
                        pbPeriod.setMin(0);
                        pbPeriod.setMax((int) MONTHLY_PERIOD);
                        pbPeriod.setProgress(remainingDays);
                        // Set progress bar text
                        String remainingDaysInStr = remainingDays + " Days";
                        periodProgressBarTV.setText(remainingDaysInStr);
                        // Set period details
                        String prediction = "Likely to start on " + localDateToDateInStr(predictedDate, DATE_LONG_FORMAT);
                        periodDetailsTV.setText(prediction);
                    } else {
                        periodProgressBarTV.setText(R.string.no_record_string);
                        periodDetailsTV.setText(R.string.likely_to_start_on_string);
                    }
                }
            }
        });
    }

    private void updatePBWorkout() {
        workoutDB.get().addOnCompleteListener(task -> {
            TextView workoutProgressBarTV = findViewById(R.id.workoutProgressBarTV);
            TextView workoutDetailsTV = findViewById(R.id.workoutDetailsTV);
            HashMap tempMap = (HashMap) task.getResult().getValue();
            try {
                String oneCal = (String) ((HashMap) tempMap.get("Activity_one")).get("goal_calories");
                String twoCal = (String) ((HashMap) tempMap.get("Activity_two")).get("goal_calories");
                String threeCal = (String) ((HashMap) tempMap.get("Activity_three")).get("goal_calories");
                String fourCal = (String) ((HashMap) tempMap.get("Activity_four")).get("goal_calories");
                boolean oneFinished = (boolean) ((HashMap) tempMap.get("Activity_one")).get("goal_finished_status");
                boolean twoFinished = (boolean) ((HashMap) tempMap.get("Activity_two")).get("goal_finished_status");
                boolean threeFinished = (boolean) ((HashMap) tempMap.get("Activity_three")).get("goal_finished_status");
                boolean fourFinished = (boolean) ((HashMap) tempMap.get("Activity_four")).get("goal_finished_status");
                String total_goal_cal_str = (String) (tempMap.get("today_total_calories_goal"));
                double total_goal_cal = (double) Double.parseDouble(total_goal_cal_str);
                int total_cal = 0;
                if (oneFinished) total_cal += Integer.parseInt(oneCal);
                if (twoFinished) total_cal += Integer.parseInt(twoCal);
                if (threeFinished) total_cal += Integer.parseInt(threeCal);
                if (fourFinished) total_cal += Integer.parseInt(fourCal);
                double progress = (double) ((double) total_cal / (double) total_goal_cal);
                int int_progress = (int) (progress * 100);
                if (int_progress == 0) {
                    return;
                }
                pbWorkout.setMin(0);
                pbWorkout.setProgress(int_progress);
                String percentInStr = int_progress + "%";
                String goalStr = "Goal " + total_goal_cal_str + " Cal";
                workoutDetailsTV.setText(goalStr);
                workoutProgressBarTV.setText(percentInStr);
            } catch (Exception err) {
                workoutProgressBarTV.setText(R.string._0_percent_string);
                workoutDetailsTV.setText(R.string.goal_string);
            }
        });
    }

    private void initProgressBars() {
        pbDiet = findViewById(R.id.dietProgressBar);
        pbPeriod = findViewById(R.id.periodProgressBar);
        pbWater = findViewById(R.id.waterProgressBar);
        pbWorkout = findViewById(R.id.workoutProgressBar);
    }

    private void initProgressBarsValue() {
        pbPeriod.setMin(-1);
        pbPeriod.setProgress(-1);

        pbDiet.setMin(-1);
        pbDiet.setProgress(-1);

        pbWater.setMin(-1);
        pbWater.setProgress(-1);
        TextView waterDetailsTV = findViewById(R.id.waterDetailsTV);
        String goal = "Goal " + DAILY_WATER_TARGET_OZ + " Oz";
        waterDetailsTV.setText(goal);

        pbWorkout.setMin(-1);
        pbWorkout.setProgress(-1);
    }

    private void initProfileDrawer() {
        // Initialize profile drawer
        drawer = findViewById(R.id.drawer_layout);
        profile_nv = findViewById(R.id.nav_view_health_record);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Health Records");
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
        storage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage_reference = storage.getReference("users").child(user.getUid());

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

        update_image_from_database();
        ChangeAvartaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddPicturePressed(v);
            }
        });
    }

    //new to add
    public void update_image_from_database() {
        //if the firebase already has an image for the profile, use the existing one
        mDatabase.child("profile_image").get().addOnCompleteListener((task) -> {
            String tempStr = (String) task.getResult().getValue();
            if (tempStr == null ) {
                tempStr = "";
            }
            if (!tempStr.equals("")) {
                //update the image
                StorageReference profileReference = storage.getReferenceFromUrl(tempStr);
                profileReference.getBytes(FIVE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap currentImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Bitmap rotate =  RotateBitmap(currentImage, 90f);
                        user_image.setImageBitmap(rotate);
                    }
                });
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
            filePath = data.getData();
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            currentImageBytes = byteArray;
            Bitmap currentImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            Bitmap rotate =  RotateBitmap(currentImage, 90f);
            user_image.setImageBitmap(rotate);
            upload_image_to_cloud();
        }
    }

    public void upload_image_to_cloud() {
        long currentMillis = System.currentTimeMillis();
        final StorageReference fileRef = storage_reference.child("profile_avatar")
                .child(String.valueOf(currentMillis));
        fileRef.putBytes(currentImageBytes).addOnSuccessListener((task)-> {
            fileRef.getDownloadUrl().addOnSuccessListener((uriTask -> {
                String uriImage = uriTask.toString();
                mDatabase.child("profile_image").setValue(uriImage);
            }));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.health_record_icon);

        initProgressBarsValue();

        updatePBDiet();
        updatePBWater();
        updatePBPeriod();
        updatePBWorkout();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            if (selectedId == R.id.award_icon) {
                startActivity(new Intent(getApplicationContext(), AwardActivity.class));
                overridePendingTransition(0, 0);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        switch (item.getItemId()) {
            case R.id.nav_settings:
                drawer.closeDrawers();
                Intent i = new Intent(HealthRecordActivity.this, SettingsActivity.class);
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

    private void setDateAndWelcomeUserTextView() {
        // Display Today in string.
        String today = localDateToDateInStr(LocalDate.now(), DATE_FULL_FORMAT);
        TextView todayTV = findViewById(R.id.todayTV);
        todayTV.setText(today);

        // Display welcome user
        TextView welcomeTV = findViewById(R.id.welcomeTV);
        DatabaseReference emailRef = mDatabase.child("email");
        emailRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                String email = task.getResult().getValue(String.class);
                String welcome = "Hi, " + email;
                 welcomeTV.setText(welcome);
            }
        });
    }

    // Convert LocalDate to date in specified string format.
    private String localDateToDateInStr(LocalDate date, String dateFormat) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
        return date.format(dateTimeFormatter);
    }

    // Calculate the days between start and end, not include start or end date. Here the `start`
    // and `end` are in DATE_SHORT_FORMAT, "yyyy-mm-dd".
    private long calculateDaysBetween(String start, String end) {
        if (start.equals("") || end.equals("")) {
            return 0;
        }
        LocalDate dateBefore = LocalDate.parse(start);
        LocalDate dateAfter = LocalDate.parse(end);
        return ChronoUnit.DAYS.between(dateBefore, dateAfter);
    }
}