package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class EditWorkoutGoalActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private TextView dateInfoLabel;
    private TextView goal_calories_TX;
    private TextView goal_time;
    private int totalCalories;
    private int totalTime;
    private Toolbar toolbar;
    private DatabaseReference mDatabase;
    private Button updateBtn;
    private DatabaseReference myDataBase;
    private DatabaseReference workoutDB;
    private LocalDate selectedDate;
    private Spinner workout_numbers_selection;
    private Spinner workout_activity_choose_1;
    private Spinner workout_time_choose_1;
    private Spinner workout_activity_choose_2;
    private Spinner workout_time_choose_2;
    private Spinner workout_activity_choose_3;
    private Spinner workout_time_choose_3;
    private Spinner workout_activity_choose_4;
    private Spinner workout_time_choose_4;
    private int totalNumberOfWorkout;
    private CardView CV1;
    private CardView CV2;
    private CardView CV3;
    private CardView CV4;
    private HashMap<String, Integer> activitiesCalories;
    private int activity_1_calories;
    private int activity_2_calories;
    private int activity_3_calories;
    private int activity_4_calories;
    private int activity_1_time;
    private int activity_2_time;
    private int activity_3_time;
    private int activity_4_time;
    private int prev_total_cal_1;
    private int prev_total_cal_2;
    private int prev_total_cal_3;
    private int prev_total_cal_4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout_goal);
        //setup the bottom nav bar
        initWidgets();
        setBottomNavigationView();

        // Get the current user from firebase authentication.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Set up the firebase Database reference.
        assert user != null;

        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        // Initialize the selected date as today.
        selectedDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL dd yyyy");
        String formattedDateString = selectedDate.format(formatter);
        workoutDB = mDatabase.child("work-out").child(formattedDateString);

        //set up the date for today
        initDate();
        goal_calories_TX = findViewById(R.id.textView_show_workout_calories);
        goal_time = findViewById(R.id.textView_show_workout_time);
        updateBtn = findViewById(R.id.button_submit_goal);

        //set up the toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Workout Goals for Today");

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
        activity_1_calories = 0;
        activity_2_calories = 0;
        activity_3_calories = 0;
        activity_4_calories = 0;
        activity_1_time = 0;
        activity_2_time = 0;
        activity_3_time = 0;
        activity_4_time = 0;
        prev_total_cal_1 = 0;
        prev_total_cal_2 = 0;
        prev_total_cal_3 = 0;
        prev_total_cal_4 = 0;
        totalTime = 0;
        totalCalories = 0;

        //set up the activities calories burned
        activitiesCalories = new HashMap<String, Integer>();
        activitiesCalories.put("Swimming",430);
        activitiesCalories.put("Running",606);
        activitiesCalories.put("Walking",250);
        activitiesCalories.put("Hiking",430);
        activitiesCalories.put("Jogging",450);
        activitiesCalories.put("Biking",450);
        activitiesCalories.put("Dancing",370);
        activitiesCalories.put("Playing Badminton",300);
        activitiesCalories.put("Playing Basketball",558);
        activitiesCalories.put("Playing Frisbee",220);
        activitiesCalories.put("HIIT",686);

        //set up the workout number selection
        workout_numbers_selection = findViewById(R.id.spinner_select_activity_numbers);
        ArrayAdapter<CharSequence> workout_numbers_selection_adapter = ArrayAdapter.createFromResource(this, R.array.workout_activity_numbers, android.R.layout.simple_spinner_item);
        workout_numbers_selection_adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        workout_numbers_selection.setAdapter(workout_numbers_selection_adapter);

        updateTotalTimeAndCalories();

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

        //set up the activities spinners
        workout_activity_choose_1 = findViewById(R.id.select_activity_goal_1);
        workout_time_choose_1 = findViewById(R.id.select_min_goal_1);
        workout_activity_choose_2 = findViewById(R.id.select_activity_goal_2);
        workout_time_choose_2 = findViewById(R.id.select_min_goal_2);
        workout_activity_choose_3 = findViewById(R.id.select_activity_goal_3);
        workout_time_choose_3 = findViewById(R.id.select_min_goal_3);
        workout_activity_choose_4 = findViewById(R.id.select_activity_goal_4);
        workout_time_choose_4 = findViewById(R.id.select_min_goal_4);

        ArrayAdapter<CharSequence> workout_activity_selection_adapter = ArrayAdapter.createFromResource(this, R.array.workout_activities, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> workout_time_selection_adapter = ArrayAdapter.createFromResource(this, R.array.workout_time_range, android.R.layout.simple_spinner_item);

        workout_activity_choose_1.setAdapter(workout_activity_selection_adapter);
        workout_activity_choose_2.setAdapter(workout_activity_selection_adapter);
        workout_activity_choose_3.setAdapter(workout_activity_selection_adapter);
        workout_activity_choose_4.setAdapter(workout_activity_selection_adapter);
        workout_time_choose_1.setAdapter(workout_time_selection_adapter);
        workout_time_choose_2.setAdapter(workout_time_selection_adapter);
        workout_time_choose_3.setAdapter(workout_time_selection_adapter);
        workout_time_choose_4.setAdapter(workout_time_selection_adapter);

        workout_activity_choose_1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = workout_activity_choose_1.getSelectedItem().toString();
                System.out.println("text is "+ text);

                if (!text.equals("choose workout activity")) {
                    //update the card view UI
                    TextView cardActivity_show = findViewById(R.id.workoutDetail_title_01);
                    cardActivity_show.setText(text);

                    //store data, connect with database to calculate the calories
                    int oneHourCalories = (int) activitiesCalories.get(text);
                    activity_1_calories = oneHourCalories;
                    System.out.println("after choose running, calories ia "+activity_1_calories);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        workout_activity_choose_2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = workout_activity_choose_2.getSelectedItem().toString();
                System.out.println("text is "+ text);

                if (!text.equals("choose workout activity")) {
                    //update the card view UI
                    TextView cardActivity_show = findViewById(R.id.workoutDetail_title_02);
                    cardActivity_show.setText(text);

                    //store data, connect with database to calculate the calories
                    int oneHourCalories = (int) activitiesCalories.get(text);
                    activity_2_calories = oneHourCalories;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        workout_activity_choose_3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = workout_activity_choose_3.getSelectedItem().toString();
                System.out.println("text is "+ text);

                if (!text.equals("choose workout activity")) {
                    //update the card view UI
                    TextView cardActivity_show = findViewById(R.id.workoutDetail_title_03);
                    cardActivity_show.setText(text);

                    //store data, connect with database to calculate the calories
                    int oneHourCalories = (int) activitiesCalories.get(text);
                    activity_3_calories = oneHourCalories;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        workout_activity_choose_4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = workout_activity_choose_4.getSelectedItem().toString();
                System.out.println("text is "+ text);

                if (!text.equals("choose workout activity")) {
                    //update the card view UI
                    TextView cardActivity_show = findViewById(R.id.workoutDetail_title_04);
                    cardActivity_show.setText(text);

                    //store data, connect with database to calculate the calories
                    int oneHourCalories = (int) activitiesCalories.get(text);
                    activity_4_calories = oneHourCalories;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        workout_time_choose_1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = workout_time_choose_1.getSelectedItem().toString();

                //update the card view UI
                TextView cardTime_show = findViewById(R.id.workoutDetail_min_01);
                cardTime_show.setText(text);

                if (!text.equals("choose time")) {
                    //store data, connect with database to calculate the calories
                    String parts[] = text.split(" ", 2);
                    text = parts[0];
                    int timeForActivity = Integer.parseInt(text);
                    totalTime -= activity_1_time;
                    totalCalories -= prev_total_cal_1;
                    activity_1_time = timeForActivity;
                    if (activity_1_calories != 0 && activity_1_time != 0) {
                        //double percentage = activity_1_time / 60;
                        float percentage = activity_1_time *  100f/ 60;
                        percentage = percentage / 100;
                        int currentCalories = (int) (percentage * activity_1_calories);
                        totalCalories += currentCalories;
                        totalTime += activity_1_time;
                        prev_total_cal_1 = currentCalories;
                        updateTotalTimeAndCalories();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        workout_time_choose_2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = workout_time_choose_2.getSelectedItem().toString();
                System.out.println("text is "+ text);

                //update the card view UI
                TextView cardTime_show = findViewById(R.id.workoutDetail_min_02);
                cardTime_show.setText(text);

                if (!text.equals("choose time")) {
                    //store data, connect with database to calculate the calories
                    String parts[] = text.split(" ", 2);
                    text = parts[0];
                    int timeForActivity = Integer.parseInt(text);
                    totalTime -= activity_2_time;
                    totalCalories -= prev_total_cal_2;
                    activity_2_time = timeForActivity;
                    if (activity_2_calories != 0 && activity_2_time != 0) {
                        float percentage = activity_2_time *  100f/ 60;
                        percentage = percentage / 100;
                        int currentCalories = (int) (percentage * activity_2_calories);
                        totalCalories += currentCalories;
                        totalTime += activity_2_time;
                        prev_total_cal_2 = currentCalories;
                        updateTotalTimeAndCalories();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        workout_time_choose_3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = workout_time_choose_3.getSelectedItem().toString();
                System.out.println("text is "+ text);

                //update the card view UI
                TextView cardTime_show = findViewById(R.id.workoutDetail_min_03);
                cardTime_show.setText(text);

                if (!text.equals("choose time")) {
                    //store data, connect with database to calculate the calories
                    String parts[] = text.split(" ", 2);
                    text = parts[0];
                    int timeForActivity = Integer.parseInt(text);
                    totalTime -= activity_3_time;
                    totalCalories -= prev_total_cal_3;
                    activity_3_time = timeForActivity;
                    if (activity_3_calories != 0 && activity_3_time != 0) {
                        float percentage = activity_3_time *  100f/ 60;
                        percentage = percentage / 100;
                        int currentCalories = (int) (percentage * activity_3_calories);
                        totalCalories += currentCalories;
                        totalTime += activity_3_time;
                        prev_total_cal_3 = currentCalories;
                        updateTotalTimeAndCalories();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        workout_time_choose_4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = workout_time_choose_4.getSelectedItem().toString();
                System.out.println("text is "+ text);

                //update the card view UI
                TextView cardTime_show = findViewById(R.id.workoutDetail_min_04);
                cardTime_show.setText(text);

                if (!text.equals("choose time")) {
                    //store data, connect with database to calculate the calories
                    String parts[] = text.split(" ", 2);
                    text = parts[0];
                    int timeForActivity = Integer.parseInt(text);
                    totalTime -= activity_4_time;
                    totalCalories -= prev_total_cal_4;
                    activity_4_time = timeForActivity;
                    if (activity_4_calories != 0 && activity_4_time != 0) {
                        float percentage = activity_4_time *  100f/ 60;
                        percentage = percentage / 100;
                        int currentCalories = (int) (percentage * activity_4_calories);
                        totalCalories += currentCalories;
                        totalTime += activity_4_time;
                        prev_total_cal_4 = currentCalories;
                        updateTotalTimeAndCalories();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passDataToWorkout();
            }
        });
    }

    private void passDataToWorkout() {
        Intent i = new Intent(this,
                WorkoutActivity.class);
        
        String total_goal_calories = goal_calories_TX.getText().toString();
        String numberOfActivities = String.valueOf(totalNumberOfWorkout);
        TextView activity_one = findViewById(R.id.workoutDetail_title_01);
        String activity_one_label = activity_one.getText().toString();
        TextView activity_two = findViewById(R.id.workoutDetail_title_02);
        String activity_two_label = activity_two.getText().toString();
        TextView activity_three = findViewById(R.id.workoutDetail_title_03);
        String activity_three_label = activity_three.getText().toString();
        TextView activity_four = findViewById(R.id.workoutDetail_title_04);
        String activity_four_label = activity_four.getText().toString();

        TextView activity_time_one = findViewById(R.id.workoutDetail_min_01);
        String activity_time_Label_one = activity_time_one.getText().toString();
        TextView activity_time_two = findViewById(R.id.workoutDetail_min_02);
        String activity_time_Label_two = activity_time_two.getText().toString();
        TextView activity_time_three = findViewById(R.id.workoutDetail_min_03);
        String activity_time_Label_three = activity_time_three.getText().toString();
        TextView activity_time_four = findViewById(R.id.workoutDetail_min_04);
        String activity_time_Label_four = activity_time_four.getText().toString();

        String activity_calories_one = String.valueOf(prev_total_cal_1);
        String activity_calories_two = String.valueOf(prev_total_cal_2);
        String activity_calories_three = String.valueOf(prev_total_cal_3);
        String activity_calories_four = String.valueOf(prev_total_cal_4);

        //Create the bundle
        Bundle bundle = new Bundle();

        //Add your data to bundle
        bundle.putString("calories", total_goal_calories);
        bundle.putString("activities", numberOfActivities);
        bundle.putString("activity_one", activity_one_label);
        bundle.putString("activity_two", activity_two_label);
        bundle.putString("activity_three", activity_three_label);
        bundle.putString("activity_four", activity_four_label);
        bundle.putString("activity_time_one", activity_time_Label_one);
        bundle.putString("activity_time_two", activity_time_Label_two);
        bundle.putString("activity_time_three", activity_time_Label_three);
        bundle.putString("activity_time_four", activity_time_Label_four);
        bundle.putString("activity_calories_four", activity_calories_four);
        bundle.putString("activity_calories_one", activity_calories_one);
        bundle.putString("activity_calories_two", activity_calories_two);
        bundle.putString("activity_calories_three", activity_calories_three);

        //set update on firebase
        workoutDB.child("Goal_updated").setValue(true);
        workoutDB.child("Activity_one").child("activity_type").setValue(activity_one_label);
        workoutDB.child("Activity_one").child("goal_time").setValue(activity_time_Label_one);
        workoutDB.child("Activity_one").child("goal_calories").setValue(activity_calories_one);
        workoutDB.child("Activity_one").child("goal_finished_status").setValue(false);

        workoutDB.child("Activity_two").child("activity_type").setValue(activity_two_label);
        workoutDB.child("Activity_two").child("goal_time").setValue(activity_time_Label_two);
        workoutDB.child("Activity_two").child("goal_calories").setValue(activity_calories_two);
        workoutDB.child("Activity_two").child("goal_finished_status").setValue(false);

        workoutDB.child("Activity_three").child("activity_type").setValue(activity_three_label);
        workoutDB.child("Activity_three").child("goal_time").setValue(activity_time_Label_three);
        workoutDB.child("Activity_three").child("goal_calories").setValue(activity_calories_three);
        workoutDB.child("Activity_three").child("goal_finished_status").setValue(false);

        workoutDB.child("Activity_four").child("activity_type").setValue(activity_four_label);
        workoutDB.child("Activity_four").child("goal_time").setValue(activity_time_Label_four);
        workoutDB.child("Activity_four").child("goal_calories").setValue(activity_calories_four);
        workoutDB.child("Activity_four").child("goal_finished_status").setValue(false);

        //Add the bundle to the intent
        i.putExtras(bundle);

        //Fire that second activity
        startActivity(i);
    }

    private void updateTotalTimeAndCalories() {
        String tempCalories =  String.valueOf(totalCalories);
        goal_calories_TX.setText(tempCalories);
        String tempTime =  String.valueOf(totalTime);
        goal_time.setText(tempTime);
    }

    private void updateCardViewNumber() {
        selectedDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL dd yyyy");
        String formattedDateString = selectedDate.format(formatter);
        DatabaseReference work_out_Ref = mDatabase.child("work-out");
        String TOTAL_ACTIVITIES = String.valueOf(totalNumberOfWorkout);
        work_out_Ref.child(formattedDateString).child("total_activity_number").setValue(TOTAL_ACTIVITIES);
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