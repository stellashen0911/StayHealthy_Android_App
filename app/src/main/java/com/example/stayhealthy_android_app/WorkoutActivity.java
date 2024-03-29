package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.stayhealthy_android_app.Period.Model.PeriodData;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.internal.MaterialCheckable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
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
    private DatabaseReference mDatabase;
    private LocalDate selectedDate;
    private DatabaseReference work_out_Ref;
    private DatabaseReference Activity_Ref_one;
    private DatabaseReference Activity_Ref_two;
    private DatabaseReference Activity_Ref_three;
    private DatabaseReference Activity_Ref_four;
    int goal_calories;
    int completed_calories;
    private CardView CV1;
    private CardView CV2;
    private CardView CV3;
    private CardView CV4;
    private String TOTAL_WORKOUT_CALORIES;
    private String TOTAL_ACTIVITIES;
    private String ActivityOneLabel;
    private String ActivityTwoLabel;
    private String ActivityThreeLabel;
    private String ActivityFourLabel;
    private String ActivityOneTime;
    private String ActivityTwoTime;
    private String ActivityThreeTime;
    private String ActivityFourTime;
    private int Activity_Calories_one;
    private int Activity_Calories_two;
    private int Activity_Calories_three;
    private int Activity_Calories_four;
    private CheckBox activity_checkbox_one;
    private CheckBox activity_checkbox_two;
    private CheckBox activity_checkbox_three;
    private CheckBox activity_checkbox_four;
    private DatabaseReference workoutDB;
    private Boolean updated_goal_boolean;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

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
        TOTAL_ACTIVITIES = "2";
        TOTAL_WORKOUT_CALORIES = "514";

        //setup the bottom nav bar
        initWidgets();
        setBottomNavigationView();

        //initialize the date and the goal without setting
        initDate();
        update_goal_progress_bar();

        //initialize the values state
        ActivityOneLabel = "";
        ActivityTwoLabel = "";
        ActivityThreeLabel = "";
        ActivityFourLabel = "";
        ActivityOneTime = "0";
        ActivityTwoTime = "0";
        ActivityThreeTime = "0";
        ActivityFourTime = "0";
        Activity_Calories_one = 0;
        Activity_Calories_two = 0;
        Activity_Calories_three = 0;
        Activity_Calories_four = 0;

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
        goal_calories_TX = findViewById(R.id.CaloriesGoal);
        completed_calories_TX = findViewById(R.id.GoalFinishedNumber);

        //set up the card view of detailed workout
        CV1 = (CardView) findViewById(R.id.workoutDataCV_01);
        CV2 = (CardView) findViewById(R.id.workoutDataCV_02);
        CV3 = (CardView) findViewById(R.id.workoutDataCV_03);
        CV4 = (CardView) findViewById(R.id.workoutDataCV_04);
        Activity_Ref_one = workoutDB.child("Activity_one");
        Activity_Ref_two = workoutDB.child("Activity_two");
        Activity_Ref_three = workoutDB.child("Activity_three");
        Activity_Ref_four = workoutDB.child("Activity_four");

        //Get the bundle
        Bundle bundle = getIntent().getExtras();

        activity_checkbox_one = (CheckBox) findViewById(R.id.workoutDetail_checkbox_01);
        activity_checkbox_one.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    completed_calories += Activity_Calories_one;
                    String completed_workout_calories = String.valueOf(completed_calories);
                    completed_calories_TX.setText(completed_workout_calories);
                    update_goal_progress_bar();
                    Activity_Ref_one.child("goal_finished_status").setValue(true);
                    //check whether all the activities are finished
                    check_today_goal_complete();
                } else {
                    completed_calories -= Activity_Calories_one;
                    String completed_workout_calories = String.valueOf(completed_calories);
                    completed_calories_TX.setText(completed_workout_calories);
                    update_goal_progress_bar();
                    Activity_Ref_one.child("goal_finished_status").setValue(false);
                    check_today_goal_complete();
                }
            }
        });

        activity_checkbox_two = (CheckBox) findViewById(R.id.workoutDetail_checkbox_02);
        activity_checkbox_two.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    completed_calories += Activity_Calories_two;
                    String completed_workout_calories = String.valueOf(completed_calories);
                    completed_calories_TX.setText(completed_workout_calories);
                    update_goal_progress_bar();
                    Activity_Ref_two.child("goal_finished_status").setValue(true);
                    check_today_goal_complete();
                } else {
                    completed_calories -= Activity_Calories_two;
                    String completed_workout_calories = String.valueOf(completed_calories);
                    completed_calories_TX.setText(completed_workout_calories);
                    update_goal_progress_bar();
                    Activity_Ref_two.child("goal_finished_status").setValue(false);
                    check_today_goal_complete();
                }
            }
        });

        activity_checkbox_three = (CheckBox) findViewById(R.id.workoutDetail_checkbox_03);
        activity_checkbox_three.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    completed_calories += Activity_Calories_three;
                    String completed_workout_calories = String.valueOf(completed_calories);
                    completed_calories_TX.setText(completed_workout_calories);
                    update_goal_progress_bar();
                    Activity_Ref_three.child("goal_finished_status").setValue(true);
                    check_today_goal_complete();
                } else {
                    completed_calories -= Activity_Calories_three;
                    String completed_workout_calories = String.valueOf(completed_calories);
                    completed_calories_TX.setText(completed_workout_calories);
                    update_goal_progress_bar();
                    Activity_Ref_three.child("goal_finished_status").setValue(false);
                    check_today_goal_complete();
                }
            }
        });

        activity_checkbox_four = (CheckBox) findViewById(R.id.workoutDetail_checkbox_04);
        activity_checkbox_four.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    completed_calories += Activity_Calories_four;
                    String completed_workout_calories = String.valueOf(completed_calories);
                    completed_calories_TX.setText(completed_workout_calories);
                    Activity_Ref_four.child("goal_finished_status").setValue(true);
                    check_today_goal_complete();
                    update_goal_progress_bar();
                } else {
                    completed_calories -= Activity_Calories_four;
                    String completed_workout_calories = String.valueOf(completed_calories);
                    completed_calories_TX.setText(completed_workout_calories);
                    update_goal_progress_bar();
                    Activity_Ref_four.child("goal_finished_status").setValue(false);
                    check_today_goal_complete();
                }
            }
        });

//        workoutDB.get().addOnCompleteListener(task -> {
//            HashMap tempMap = (HashMap) task.getResult().getValue();
//            try {
//                updated_goal_boolean = (boolean) tempMap.get("Goal_updated");
//                if (updated_goal_boolean == true) {
//                    System.out.println("updated start");
//                    pull_data_from_database();
//                } else {
//                    default_workout_goal();
//                }
//            } catch (Exception err) {
//                default_workout_goal();
//            }
//        });

        default_workout_goal();

        if (bundle != null) {
            //Extract the data…
            TOTAL_WORKOUT_CALORIES = bundle.getString("calories");
            TOTAL_ACTIVITIES = bundle.getString("activities");
            ActivityOneLabel = bundle.getString("activity_one");
            ActivityTwoLabel = bundle.getString("activity_two");
            ActivityThreeLabel = bundle.getString("activity_three");
            ActivityFourLabel = bundle.getString("activity_four");
            ActivityOneTime = bundle.getString("activity_time_one");
            ActivityTwoTime = bundle.getString("activity_time_two");
            ActivityThreeTime = bundle.getString("activity_time_three");
            ActivityFourTime = bundle.getString("activity_time_four");
            Activity_Calories_one = Integer.parseInt(bundle.getString("activity_calories_one"));
            Activity_Calories_two = Integer.parseInt(bundle.getString("activity_calories_two"));
            Activity_Calories_three = Integer.parseInt(bundle.getString("activity_calories_three"));
            Activity_Calories_four = Integer.parseInt(bundle.getString("activity_calories_four"));
            goal_calories_TX.setText(TOTAL_WORKOUT_CALORIES);
            updateCardViewNumber();
            update_firebase_setup();
            update_Goal_CardView();
        }

    }

    public void pull_data_from_database() {
        System.out.println("updated check box");
        workoutDB.get().addOnCompleteListener(task -> {
            HashMap tempMap = (HashMap) task.getResult().getValue();
            TOTAL_ACTIVITIES = (String) tempMap.get("total_activity_number");
            int activity_number = Integer.parseInt(TOTAL_ACTIVITIES);

            String oneCal = (String) ((HashMap) tempMap.get("Activity_one")).get("goal_calories");
            String twoCal = (String) ((HashMap) tempMap.get("Activity_two")).get("goal_calories");
            ActivityOneLabel = (String) ((HashMap) tempMap.get("Activity_one")).get("activity_type");
            ActivityTwoLabel = (String) ((HashMap) tempMap.get("Activity_two")).get("activity_type");
            ActivityOneTime = (String) ((HashMap) tempMap.get("Activity_one")).get("goal_time");
            ActivityTwoTime = (String) ((HashMap) tempMap.get("Activity_two")).get("goal_time");
            Activity_Calories_two = Integer.parseInt(twoCal);
            Activity_Calories_one = Integer.parseInt(oneCal);
            String threeCal = "";
            boolean threeFinished = false;
            String fourCal = "";
            boolean fourFinished = false;
            boolean oneFinished = (boolean) ((HashMap) tempMap.get("Activity_one")).get("goal_finished_status");
            boolean twoFinished = (boolean) ((HashMap) tempMap.get("Activity_two")).get("goal_finished_status");
            if (activity_number == 3) {
                System.out.println("here 3");

                threeFinished = (boolean) ((HashMap) tempMap.get("Activity_three")).get("goal_finished_status");
                if (threeFinished) activity_checkbox_three.setChecked(true);
                threeCal = (String) ((HashMap) tempMap.get("Activity_three")).get("goal_calories");
                Activity_Calories_three = Integer.parseInt(threeCal);
                ActivityThreeLabel = (String) ((HashMap) tempMap.get("Activity_three")).get("activity_type");
                ActivityThreeTime = (String) ((HashMap) tempMap.get("Activity_three")).get("goal_time");
                TextView workout_Activity_three = findViewById(R.id.workoutDetail_title_03);
                workout_Activity_three.setText(ActivityThreeLabel);
                TextView workout_Activity_Time_three = findViewById(R.id.workoutDetail_min_03);
                workout_Activity_Time_three.setText(ActivityThreeTime);
            }else if (activity_number == 4) {
                System.out.println("here 4");
                threeFinished = (boolean) ((HashMap) tempMap.get("Activity_three")).get("goal_finished_status");
                 fourFinished = (boolean) ((HashMap) tempMap.get("Activity_four")).get("goal_finished_status");
                System.out.println(" 4 - 1");
                if (threeFinished) activity_checkbox_three.setChecked(true);
                if (fourFinished) activity_checkbox_four.setChecked(true);
                System.out.println(" 4 - 2");
                threeCal = (String) ((HashMap) tempMap.get("Activity_three")).get("goal_calories");
                fourCal = (String) ((HashMap) tempMap.get("Activity_four")).get("goal_calories");
                System.out.println(" 4 - 3");
                ActivityThreeLabel = (String) ((HashMap) tempMap.get("Activity_three")).get("activity_type");
                ActivityFourLabel = (String) ((HashMap) tempMap.get("Activity_four")).get("activity_type");
                Activity_Calories_four = Integer.parseInt(fourCal);
                Activity_Calories_three = Integer.parseInt(threeCal);
                System.out.println(" 4 - 4");
                ActivityThreeTime = (String) ((HashMap) tempMap.get("Activity_three")).get("goal_time");
                ActivityFourTime = (String) ((HashMap) tempMap.get("Activity_four")).get("goal_time");
                System.out.println(" 4 - 5");
                TextView workout_Activity_three = findViewById(R.id.workoutDetail_title_03);
                workout_Activity_three.setText(ActivityThreeLabel);
                TextView workout_Activity_four = findViewById(R.id.workoutDetail_title_04);
                workout_Activity_four.setText(ActivityFourLabel);
                TextView workout_Activity_Time_three = findViewById(R.id.workoutDetail_min_03);
                workout_Activity_Time_three.setText(ActivityThreeTime);
                TextView workout_Activity_Time_four = findViewById(R.id.workoutDetail_min_04);
                workout_Activity_Time_four.setText(ActivityFourTime);
            }
            System.out.println("here 5");

            if (oneFinished) activity_checkbox_one.setChecked(true);
            if (twoFinished) activity_checkbox_two.setChecked(true);
            String total_goal_cal_str = (String) (tempMap.get("today_total_calories_goal"));
            double total_goal_cal = (double) Double.parseDouble(total_goal_cal_str);
            int total_cal = 0;
            if (oneFinished) total_cal += Integer.parseInt(oneCal);
            if (twoFinished) total_cal += Integer.parseInt(twoCal);
            if (threeFinished) total_cal += Integer.parseInt(threeCal);
            if (fourFinished) total_cal += Integer.parseInt(fourCal);
            goal_calories_TX.setText(total_goal_cal_str);
            completed_calories_TX.setText(String.valueOf(total_cal));
            double progress = (double) ((double) total_cal / (double) total_goal_cal);
            int int_progress = (int) (progress * 100);
            if (int_progress == 0) {
                return;
            }
            //set up the card view activity label
            TextView workout_Activity_one = findViewById(R.id.workoutDetail_title_01);
            workout_Activity_one.setText(ActivityOneLabel);
            TextView workout_Activity_two = findViewById(R.id.workoutDetail_title_02);
            workout_Activity_two.setText(ActivityTwoLabel);
            //set up the card view activity time
            TextView workout_Activity_Time_one = findViewById(R.id.workoutDetail_min_01);
            workout_Activity_Time_one.setText(ActivityOneTime);
            TextView workout_Activity_Time_two = findViewById(R.id.workoutDetail_min_02);
            workout_Activity_Time_two.setText(ActivityTwoTime);
            completed_calories = total_cal;
            goal_calories = Integer.parseInt(total_goal_cal_str);
            System.out.println("here 6");
            update_goal_progress_bar();
            System.out.println("here 7");
        });
        System.out.println("updated check finished");
    }

    public void default_workout_goal() {
        CV3.setVisibility(View.GONE);
        CV4.setVisibility(View.GONE);
        ActivityOneLabel = "Swimming";
        ActivityTwoLabel = "HIIT";
        Activity_Calories_one = 286;
        Activity_Calories_two = 228;
        ActivityOneTime = "40 min";
        ActivityTwoTime = "20 min";
        goal_calories_TX = findViewById(R.id.CaloriesGoal);
        completed_calories_TX = findViewById(R.id.GoalFinishedNumber);
        goal_calories_TX.setText(TOTAL_WORKOUT_CALORIES);
        completed_calories_TX.setText("0");
        goal_calories = Integer.parseInt(goal_calories_TX.getText().toString());
        completed_calories = Integer.parseInt(completed_calories_TX.getText().toString());
        update_goal_progress_bar();
        work_out_Ref = mDatabase.child("work-out");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL dd yyyy");
        String formattedDateString = selectedDate.format(formatter);
        work_out_Ref.child(formattedDateString).child("total_activity_number").setValue(TOTAL_ACTIVITIES);
        Activity_Ref_one = work_out_Ref.child(formattedDateString).child("Activity_one");
        Activity_Ref_two = work_out_Ref.child(formattedDateString).child("Activity_two");
        Activity_Ref_three = work_out_Ref.child(formattedDateString).child("Activity_three");
        Activity_Ref_four = work_out_Ref.child(formattedDateString).child("Activity_four");
        Activity_Ref_one.child("activity_type").setValue(ActivityOneLabel);
        Activity_Ref_two.child("activity_type").setValue(ActivityTwoLabel);
        Activity_Ref_one.child("goal_calories").setValue(String.valueOf(Activity_Calories_one));
        Activity_Ref_two.child("goal_calories").setValue(String.valueOf(Activity_Calories_two));
        Activity_Ref_one.child("goal_time").setValue(ActivityOneTime);
        Activity_Ref_two.child("goal_time").setValue(ActivityTwoTime);
        Activity_Ref_four.child("goal_finished_status").setValue(false);
        Activity_Ref_three.child("goal_finished_status").setValue(false);
        Activity_Ref_one.child("goal_finished_status").setValue(false);
        Activity_Ref_two.child("goal_finished_status").setValue(false);
        //update the total
        goal_calories_TX.setText(TOTAL_WORKOUT_CALORIES);
        completed_calories_TX.setText("0");
        goal_calories = Integer.parseInt(goal_calories_TX.getText().toString());
        completed_calories = Integer.parseInt(completed_calories_TX.getText().toString());
        update_goal_progress_bar();
        updateCardViewNumber();
        workoutDB.child("Goal_updated").setValue(false);
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

    private void update_firebase_setup() {
        // Get the current user from firebase authentication.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Set up the firebase Database reference.
        assert user != null;
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        // Initialize the selected date as today.
        selectedDate = LocalDate.now();

        work_out_Ref = mDatabase.child("work-out");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL dd yyyy");
        String formattedDateString = selectedDate.format(formatter);
        Activity_Ref_one = work_out_Ref.child(formattedDateString).child("Activity_one");
        Activity_Ref_two = work_out_Ref.child(formattedDateString).child("Activity_two");
        Activity_Ref_three = work_out_Ref.child(formattedDateString).child("Activity_three");
        Activity_Ref_four = work_out_Ref.child(formattedDateString).child("Activity_four");
    }

    private void update_firebase_data(int activity_number, String activity_time, String activity_label, int activity_cal_goal) {
        DatabaseReference current_activity;
        if (activity_number == 1) {
            current_activity = Activity_Ref_one;
        } else if (activity_number == 2) {
            current_activity = Activity_Ref_two;
        } else if (activity_number == 3) {
            current_activity = Activity_Ref_three;
        } else if (activity_number == 4) {
            current_activity = Activity_Ref_four;
        } else {
            return;
        }

        String cal_update = String.valueOf(activity_cal_goal);
        current_activity.child("activity_type").setValue(activity_label);
        current_activity.child("goal_calories").setValue(cal_update);
        current_activity.child("goal_time").setValue(activity_time);
        current_activity.child("goal_finished_status").setValue(false);
    }

    private void update_Goal_CardView() {
        updateCardViewNumber();

        //set up the card view activity label
        TextView workout_Activity_one = findViewById(R.id.workoutDetail_title_01);
        workout_Activity_one.setText(ActivityOneLabel);
        TextView workout_Activity_two = findViewById(R.id.workoutDetail_title_02);
        workout_Activity_two.setText(ActivityTwoLabel);
        TextView workout_Activity_three = findViewById(R.id.workoutDetail_title_03);
        workout_Activity_three.setText(ActivityThreeLabel);
        TextView workout_Activity_four = findViewById(R.id.workoutDetail_title_04);
        workout_Activity_four.setText(ActivityFourLabel);

        //set up the card view activity time
        TextView workout_Activity_Time_one = findViewById(R.id.workoutDetail_min_01);
        workout_Activity_Time_one.setText(ActivityOneTime);
        TextView workout_Activity_Time_two = findViewById(R.id.workoutDetail_min_02);
        workout_Activity_Time_two.setText(ActivityTwoTime);
        TextView workout_Activity_Time_three = findViewById(R.id.workoutDetail_min_03);
        workout_Activity_Time_three.setText(ActivityThreeTime);
        TextView workout_Activity_Time_four = findViewById(R.id.workoutDetail_min_04);
        workout_Activity_Time_four.setText(ActivityFourTime);


        //update the firebase data
        update_firebase_data(1, ActivityOneTime, ActivityOneLabel, Activity_Calories_one);
        update_firebase_data(2, ActivityTwoTime, ActivityTwoLabel, Activity_Calories_two);
        update_firebase_data(3, ActivityThreeTime, ActivityThreeLabel, Activity_Calories_three);
        update_firebase_data(4, ActivityFourTime, ActivityFourLabel, Activity_Calories_four);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL dd yyyy");
        String formattedDateString = selectedDate.format(formatter);
        work_out_Ref.child(formattedDateString).child("today_total_calories_goal").setValue(TOTAL_WORKOUT_CALORIES);
        work_out_Ref.child(formattedDateString).child("today_goal_finished_status").setValue(false);
    }

    private void updateCardViewNumber() {
        int total_activities = Integer.parseInt(TOTAL_ACTIVITIES);
        if (total_activities == 1) {
            CV1.setVisibility(View.VISIBLE);
            CV2.setVisibility(View.GONE);
            CV3.setVisibility(View.GONE);
            CV4.setVisibility(View.GONE);
        } else if (total_activities == 2) {
            CV1.setVisibility(View.VISIBLE);
            CV2.setVisibility(View.VISIBLE);
            CV3.setVisibility(View.GONE);
            CV4.setVisibility(View.GONE);
        } else if (total_activities == 3) {
            CV1.setVisibility(View.VISIBLE);
            CV2.setVisibility(View.VISIBLE);
            CV3.setVisibility(View.VISIBLE);
            CV4.setVisibility(View.GONE);
        } else if (total_activities == 4) {
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

    private void check_today_goal_complete() {
        int activity_numbers = Integer.parseInt(TOTAL_ACTIVITIES);
        System.out.println(activity_numbers);
        if (activity_numbers == 1) {
            workoutDB.get().addOnCompleteListener(task -> {
                HashMap tempMap = (HashMap) task.getResult().getValue();
                boolean activity_one_status = (boolean) ((HashMap)tempMap.get("Activity_one")).get("goal_finished_status");
                if (activity_one_status) {
                    workoutDB.child("today_goal_finished_status").setValue(true);
                } else {
                    workoutDB.child("today_goal_finished_status").setValue(false);
                }
            });
        } else if (activity_numbers == 2) {
            workoutDB.get().addOnCompleteListener(task -> {
                HashMap tempMap = (HashMap) task.getResult().getValue();
                boolean activity_one_status = (boolean) ((HashMap)tempMap.get("Activity_one")).get("goal_finished_status");
                boolean activity_two_status = (boolean) ((HashMap)tempMap.get("Activity_two")).get("goal_finished_status");
                if (activity_one_status && activity_two_status) {
                    workoutDB.child("today_goal_finished_status").setValue(true);
                } else {
                    workoutDB.child("today_goal_finished_status").setValue(false);
                }
            });
        } else if (activity_numbers == 3) {
            workoutDB.get().addOnCompleteListener(task -> {
                HashMap tempMap = (HashMap) task.getResult().getValue();
                boolean activity_one_status = (boolean) ((HashMap) tempMap.get("Activity_one")).get("goal_finished_status");
                boolean activity_two_status = (boolean) ((HashMap) tempMap.get("Activity_two")).get("goal_finished_status");
                boolean activity_three_status = (boolean) ((HashMap) tempMap.get("Activity_three")).get("goal_finished_status");
                if (activity_one_status && activity_two_status && activity_three_status) {
                    workoutDB.child("today_goal_finished_status").setValue(true);
                } else {
                    workoutDB.child("today_goal_finished_status").setValue(false);
                }
            });
        } else if (activity_numbers == 4) {
            workoutDB.get().addOnCompleteListener(task -> {
                HashMap tempMap = (HashMap) task.getResult().getValue();
                boolean activity_one_status = (boolean) ((HashMap) tempMap.get("Activity_one")).get("goal_finished_status");
                boolean activity_two_status = (boolean) ((HashMap) tempMap.get("Activity_two")).get("goal_finished_status");
                boolean activity_three_status = (boolean) ((HashMap) tempMap.get("Activity_three")).get("goal_finished_status");
                boolean activity_four_status = (boolean) ((HashMap) tempMap.get("Activity_four")).get("goal_finished_status");
                if (activity_one_status && activity_two_status && activity_three_status && activity_four_status) {
                    workoutDB.child("today_goal_finished_status").setValue(true);
                } else {
                    workoutDB.child("today_goal_finished_status").setValue(false);
                }
            });
        }
        update_goal_progress_bar();
    }

    private void update_goal_progress_bar() {
        //initiate the progress bar
        goal_calories_TX = findViewById(R.id.CaloriesGoal);
        completed_calories_TX = findViewById(R.id.GoalFinishedNumber);
        show_percentage = findViewById(R.id.progressBar_percentage);
        progressBar = findViewById(R.id.progressBar);

        goal_calories = Integer.parseInt(goal_calories_TX.getText().toString());
        completed_calories = Integer.parseInt(completed_calories_TX.getText().toString());
        double progress = (double) ((double) completed_calories / (double) goal_calories);
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