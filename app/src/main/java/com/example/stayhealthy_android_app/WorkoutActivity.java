package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class WorkoutActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private TextView dateInfoLabel;
    private TextView goal_calories_TX;
    private TextView completed_calories_TX;
    private TextView show_percentage;
    private ProgressBar progressBar;
    private Button editWorkoutGoalBtn;
    private Toolbar toolbar;
    int goal_calories;
    int completed_calories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);


        //setup the bottom nav bar
        initWidgets();
        setBottomNavigationView();

        //initialize the date and the goal without setting
        initDate();
        update_goal();

        //set up the edit workout button
        editWorkoutGoalBtn = findViewById(R.id.button_edit_goal);
        editWorkoutGoalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditGoalActivity();
            }
        });

        //set up the toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Work Out Records");
    }

    public void openEditGoalActivity() {
        Intent EditWorkoutIntent = new Intent(this, EditWorkoutGoalActivity.class);
        startActivity(EditWorkoutIntent);
    }

    private void initDate() {
        //Initialize today's date
        dateInfoLabel = findViewById(R.id.todayDateWorkout);
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL dd yyyy");
        String formattedString = localDate.format(formatter);
        dateInfoLabel.setText(formattedString);
    }

    private void update_goal() {
        //initiate the progress bar
        System.out.println("here");
        goal_calories_TX = findViewById(R.id.CaloriesGoal);
        completed_calories_TX = findViewById(R.id.GoalFinishedNumber);
        show_percentage = findViewById(R.id.progressBar_percentage);
        progressBar = findViewById(R.id.progressBar);

        goal_calories = Integer.parseInt(goal_calories_TX.getText().toString());
        System.out.println("goal calorie is " + goal_calories);
        completed_calories = Integer.parseInt(completed_calories_TX.getText().toString());
        double progress = (double) ((double) completed_calories / (double) goal_calories);
        System.out.println("complete calorie is " + completed_calories);
        System.out.println("the progress is " + progress);
        int int_progress = (int) (progress * 100);
        progressBar.setProgress(int_progress);
        String progress_str = String.valueOf(int_progress);
        show_percentage.setText(progress_str + "%");


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