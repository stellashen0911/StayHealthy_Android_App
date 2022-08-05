package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EditWorkoutGoalActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private TextView dateInfoLabel;
    private TextView goal_calories_TX;
    private TextView goal_time;
    private Toolbar toolbar;
    private DatabaseReference myDataBase;
    private Spinner workout_numbers_selection;
    private int totalNumberOfWorkout;
    private CardView CV1;
    private CardView CV2;
    private CardView CV3;
    private CardView CV4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout_goal);
        //setup the bottom nav bar
        initWidgets();
        setBottomNavigationView();
        //set up the date for today
        initDate();
        goal_calories_TX = findViewById(R.id.textView_show_workout_calories);
        goal_time = findViewById(R.id.textView_show_workout_time);

        //set up the toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Workout Goals for Today");

        //set up the firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myDataBase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        //set up the card view of detailed workout
        CV1 = (CardView) findViewById(R.id.workoutGoalCV_01);
        CV2 = (CardView) findViewById(R.id.workoutGoalCV_02);
        CV3 = (CardView) findViewById(R.id.workoutGoalCV_03);
        CV4 = (CardView) findViewById(R.id.workoutGoalCV_04);
        CV1.setVisibility(View.GONE);
        CV2.setVisibility(View.GONE);
        CV3.setVisibility(View.GONE);
        CV4.setVisibility(View.GONE);

        //set up the workout number selection
        workout_numbers_selection = findViewById(R.id.spinner_select_activity_numbers);
        ArrayAdapter<CharSequence> workout_numbers_selection_adapter = ArrayAdapter.createFromResource(this, R.array.workout_activity_numbers, android.R.layout.simple_spinner_item);
        workout_numbers_selection_adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        workout_numbers_selection.setAdapter(workout_numbers_selection_adapter);

        workout_numbers_selection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = workout_numbers_selection.getSelectedItem().toString();
                System.out.println("text is "+ text);
                if (text.equals("1 workout activity")) {
                    totalNumberOfWorkout = 1;
                } else if (text.equals("2 workout activities")) {
                    totalNumberOfWorkout = 2;
                } else if (text.equals("3 workout activities")) {
                    totalNumberOfWorkout = 3;
                } else if (text.equals("4 workout activities")) {
                    totalNumberOfWorkout = 4;
                } else {
                    totalNumberOfWorkout = 0;
                }
                updateCardViewNumber();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                totalNumberOfWorkout = 0;
            }
        });


    }

    private void updateCardViewNumber() {
            if (totalNumberOfWorkout == 1) {
                CV1.setVisibility(View.VISIBLE);
                CV2.setVisibility(View.GONE);
                CV3.setVisibility(View.GONE);
                CV4.setVisibility(View.GONE);
            } else if (totalNumberOfWorkout == 2) {
                CV1.setVisibility(View.VISIBLE);
                CV2.setVisibility(View.VISIBLE);
                CV3.setVisibility(View.GONE);
                CV4.setVisibility(View.GONE);
            } else if (totalNumberOfWorkout == 3) {
                CV1.setVisibility(View.VISIBLE);
                CV2.setVisibility(View.VISIBLE);
                CV3.setVisibility(View.VISIBLE);
                CV4.setVisibility(View.GONE);
            } else if (totalNumberOfWorkout == 4) {
                CV1.setVisibility(View.VISIBLE);
                CV2.setVisibility(View.VISIBLE);
                CV3.setVisibility(View.VISIBLE);
                CV4.setVisibility(View.VISIBLE);
            } else {
                CV1.setVisibility(View.GONE);
                CV2.setVisibility(View.GONE);
                CV3.setVisibility(View.GONE);
                CV4.setVisibility(View.GONE);
            }
    }

    private void initDate() {
        //Initialize today's date
        dateInfoLabel = findViewById(R.id.EditWorkoutTodayDate);
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL dd yyyy");
        String formattedString = localDate.format(formatter);
        dateInfoLabel.setText(formattedString);
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