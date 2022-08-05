package com.example.stayhealthy_android_app.Period;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.AwardActivity;
import com.example.stayhealthy_android_app.JourneyActivity;
import com.example.stayhealthy_android_app.Period.Calendar.CalendarAdapter;
import com.example.stayhealthy_android_app.Period.Cycle.CycleAdapter;
import com.example.stayhealthy_android_app.Period.Model.Cycle;
import com.example.stayhealthy_android_app.Period.Model.PeriodData;
import com.example.stayhealthy_android_app.R;
import com.example.stayhealthy_android_app.databinding.ActivityPeriodBinding;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class PeriodActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{
    private final static String TAG = "MPeriodActivity";
    private final static String DATE_FULL_FORMAT = "EEEE, MMMM d, yyyy";
    private final static String MONTH_YEAR_FORMAT = "MMMM yyyy";
    private final static String DATE_LONG_FORMAT = "MMM dd yyyy";
    private final static String DATE_SHORT_FORMAT = "yyyy-MM-dd";
    private final static String SELECT_DATE_KEY = "select_key";
    private final static int DAY_TO_MILLISECONDS = 86400000; // that is: 24 * 60 * 60 * 1000
    private ActivityPeriodBinding binding;
    private DatabaseReference mDatabase;
    private BottomNavigationView bottomNavigationView;
    private LocalDate selectedDate;
    private List<String> daysOfMonth;
    private List<Integer> periodDatesInMonth;
    private List<Integer> recordedDatesInMonth;
    private List<Cycle> cycleList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPeriodBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Get the current user from firebase authentication.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Set up the firebase Database reference.
        assert user != null;
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        // Initialize the selected date as today.
        selectedDate = LocalDate.now();

        // Initialize the days of month array, period dates and recorded dates in selectedDate's
        // month. Initialize the cycle history list.
        daysOfMonth = new ArrayList<>();
        periodDatesInMonth = new ArrayList<>();
        recordedDatesInMonth = new ArrayList<>();
        cycleList = new ArrayList<>();

        // Set bottom navigation view.
        setBottomNavigationView();

        // Set divider for cycleHistory recycler view.
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        binding.cycleHistoryRV.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SELECT_DATE_KEY, selectedDate);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedDate = (LocalDate) savedInstanceState.getSerializable(SELECT_DATE_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.health_record_icon);

        // Update UI according to the selected date.
        updateUI();

        // Register flow condition radio group.
        registerFlowConditionRadioGroup();
    }

    // Interface in CalendarAdapter.OnItemListener. Used to update date by selected day on the
    // calendar recycler view.
    @Override
    public void onItemClick(int position, TextView dateTV) {
        if (!dateTV.getText().equals("")) {
            selectedDate = selectedDate.plusDays(
                    Integer.parseInt((String)dateTV.getText()) - selectedDate.getDayOfMonth());
            updateUI();
        }
    }

    // previousBTN onClickListener
    public void previousMonthAction(View view) {
        selectedDate = selectedDate.minusMonths(1).withDayOfMonth(1);
        updateUI();
    }

    // nextBTN onClickListener
    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1).withDayOfMonth(1);
        updateUI();
    }

    // monthYearBTN onClickListener. Shows a MonthYearPickerDialog where user can select a month
    // and year. Then update selectedDate, DaysOfMonthRecyclerView, DateView on the period screen
    // with user selected month and year, day is remain the same.
    public void showMonthYearPickerDialog(View v) {
        int currYear = selectedDate.getYear();
        // month parameter in DatePickerDialog constructor ranges in (0, 11)
        // LocalDate.getMonthValue() return (1, 12)
        int currMonth = selectedDate.getMonthValue();
        int currDay = selectedDate.getDayOfMonth();

        // New a month year picker dialog.
        MonthYearPickerDialog monthYearPickerDialog = MonthYearPickerDialog.newInstance(currMonth,
                currDay, currYear);

        // Listening to the user's choice
        monthYearPickerDialog.setListener((view, year, monthOfYear, dayOfMonth) -> {
            setSelectedDate(year, monthOfYear, dayOfMonth);
            updateUI();
        });

        monthYearPickerDialog.show(getSupportFragmentManager(), "MonthYearPickerDialog");
    }

    // addPeriodDateBTN onClickListener. A datePicker shown when clicked. User can choose a date or
    // a range of dates through this picker.
    public void addPeriodDate(View view) {
        // Set default selection range as 2 days ago to today. StartDate is 4 days ago before the
        // end date.
        int daysAgo = -4;
        Long defaultEndDateInMilliseconds = selectedDateInMilliseconds();
        Long defaultStartDateInMilliseconds = neighborDatesInMilliseconds(defaultEndDateInMilliseconds, daysAgo);
        final MaterialDatePicker<Pair<Long, Long>> materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Period Range")
                .setSelection(new Pair<>(defaultStartDateInMilliseconds, defaultEndDateInMilliseconds))
                .build();
        materialDatePicker.show(getSupportFragmentManager(), "PeriodDateRangePicker");

        materialDatePicker.addOnPositiveButtonClickListener(
                selection -> {
                    // Save the selected range to firebase database
                    Long startDateInMilliseconds = selection.first;
                    Long endDateInMilliseconds = selection.second;
                    savePeriodRangeToDatabase(startDateInMilliseconds, endDateInMilliseconds);

                    // Update the selected date as the first day of the range
                    String startDate = utcMillisecondsToDateInStr(startDateInMilliseconds);
                    selectedDate = LocalDate.parse(startDate);
                });
    }

    // addPeriodFlowBTN onClickListener. An alert dialog is shown where user can choose a flow level.
    public void addFlowLevel(View view) {
        String[] flowLevelOptions = getResources().getStringArray(R.array.flow_level_array);
        Integer[] checkedFlowLevel = {0};
        for (int i = 0; i < flowLevelOptions.length; i++) {
            if (binding.periodFlowDetailsTV.getText().equals(flowLevelOptions[i])) {
                checkedFlowLevel[0] = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.choose_flow_level_string)
                .setSingleChoiceItems(flowLevelOptions, checkedFlowLevel[0], (dialog, which)
                        -> checkedFlowLevel[0] = which)
                .setPositiveButton(R.string.ok_string, (dialog, which) -> {
                    String flowLevel = flowLevelOptions[checkedFlowLevel[0]];
                    binding.periodFlowDetailsTV.setText(flowLevel);
                    saveFlowLevelToDatabase(flowLevel);
                })
                .setNegativeButton(R.string.cancel_string, null)
                .create()
                .show();
    }

    // addSymptomsBTN onClickListener. An alert dialog is shown where user can choose one or more symptoms.
    public void addSymptoms(View view) {
        String[] symptomsOptions = getResources().getStringArray(R.array.symptoms_array);
        boolean[] checkedSymptoms = new boolean[symptomsOptions.length];
        ArrayList<String> symptomsList = new ArrayList<>(Arrays.asList(binding.symptomsDetailsTV.getText().toString().split(", ")));
        for (int i = 0; i < symptomsOptions.length; i++) {
            if (symptomsList.contains(symptomsOptions[i])) {
                checkedSymptoms[i] = true;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.choose_symptoms_string)
                .setMultiChoiceItems(symptomsOptions, checkedSymptoms, (dialog, i, isChecked)
                        -> checkedSymptoms[i] = isChecked)
                .setPositiveButton(R.string.ok_string, (dialog, which) -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    String prefix = "";
                    for(int i = 0; i < checkedSymptoms.length; i++) {
                        if (checkedSymptoms[i]) {
                            stringBuilder.append(prefix);
                            prefix = ", ";
                            stringBuilder.append(symptomsOptions[i]);
                        }
                    }
                    binding.symptomsDetailsTV.setText(stringBuilder);
                    saveSymptomsToDatabase(stringBuilder.toString());
                })
                .setNegativeButton(R.string.cancel_string, null)
                .create()
                .show();
    }

    // addMood onClickListener. An alert dialog is shown where user can choose a mood.
    public void addMood(View view) {
        // Create mood options string.
        String[] moodOptions = getResources().getStringArray(R.array.mood_array);
        // Set default checked item position as the index 0.
        Integer[] checkedMood = {0};
        // Set the checked item as the one shown on the moodEmojiTV.
        for (int i = 0; i < moodOptions.length; i++) {
            if (binding.moodEmojiTV.getText().equals(moodOptions[i])) {
                checkedMood[0] = i;
                break;
            }
        }

        // Create an arrayAdapter to place moodOptions and Set the checked item as gray in the background.
        ArrayAdapter<CharSequence> arrayAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_gallery_item, moodOptions) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                int color = ContextCompat.getColor(PeriodActivity.this, R.color.transparent); // Transparent
                if (position == checkedMood[0]) {
                    color = ContextCompat.getColor(PeriodActivity.this, R.color.gray); // Opaque Blue
                }
                view.setBackgroundColor(color);
                view.setPadding(20, 20, 20, 20);
                return view;
            }
        };

        // Create a grid View to place the mood options.
        GridView gridView = new GridView(this);
        gridView.setAdapter(arrayAdapter);
        gridView.setNumColumns(3);
        gridView.setGravity(Gravity.CENTER);
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            checkedMood[0] = position;
            for (int i = 0; i < moodOptions.length; i++) {
                if (i != position) {
                    View other = gridView.getChildAt(i);
                    other.setBackgroundColor(ContextCompat.getColor(PeriodActivity.this, R.color.transparent));
                } else {
                    view1.setBackgroundColor(ContextCompat.getColor(PeriodActivity.this, R.color.gray));
                }
            }});

        // Create the choose mood dialog, its content is the customized gridView
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.choose_mood_string)
                .setPositiveButton(R.string.ok_string, (dialog, which) -> {
                    binding.moodEmojiTV.setText(moodOptions[checkedMood[0]]);
                    binding.moodEmojiTV.setTextColor(ContextCompat.getColor(this, R.color.emoji_color));
                    saveMoodToDatabase(checkedMood[0]);})
                .setNegativeButton(R.string.cancel_string, null)
                .setView(gridView)
                .create()
                .show();
    }

    // Expand and collapse Cycle History Card View
    public void expandAndCollapseCycleHistory(View view) {
        if (binding.cycleHistoryRV.getVisibility() == View.GONE) {
            TransitionManager.beginDelayedTransition(binding.cycleHistoryRV, new AutoTransition());
            binding.cycleHistoryRV.setVisibility(View.VISIBLE);
            binding.cycleHistoryExpandBTN.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_expand_less_24));
        } else {
            TransitionManager.beginDelayedTransition(binding.cycleHistoryRV, new AutoTransition());
            binding.cycleHistoryRV.setVisibility(View.GONE);
            binding.cycleHistoryExpandBTN.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_expand_more_24));
        }
    }

    // Call this method every time the selected date is changed. The information displayed on the
    // screen is based on the selected date.
    private void updateUI() {
        // Disable add buttons when selected date is in the future.
        setButtonEnableCondition();
        // Set the selected date view. no need to read data.
        setDateView();
        // Set the initial calendar recycler view which displays the days of the selected month.
        setCalendarRecyclerView();
        // Read this month's period dates from database and update the adapter for calendarRV.
        getPeriodDatesInMonthAndUpdateCalendarRecyclerViewAdapter();
        // Read `hadFlow`, `flowLevel`, `symptoms` and `mood` data from database and update UI.
        setRecentPeriodView();
        setFlowConditionRadioGroup();
        setFlowLevelView();
        setSymptomsView();
        setMoodView();
        setPeriodPredictionDateView();
        // Set the initial cycle history recycler view where cycle data is empty.
        setCycleHistoryRecyclerView();
        // Read all periods data, form cycle list and update cycle history recycler view.
        getCycleHistoryAndUpdateCycleHistoryRecyclerViewAdapter();
    }

    // Update user selected date. Convert the (year, month, day) to LocalDate format.
    private void setSelectedDate(int year, int month, int day) {
        selectedDate = selectedDate.plusDays(day - selectedDate.getDayOfMonth());
        selectedDate = selectedDate.plusMonths(month - (selectedDate.getMonthValue()));
        selectedDate = selectedDate.plusYears(year - selectedDate.getYear());
    }

    private void setButtonEnableCondition() {
        boolean enabled = !selectedDate.isAfter(LocalDate.now());
        binding.addPeriodDateBTN.setEnabled(enabled);
        binding.addPeriodFlowBTN.setEnabled(enabled);
        binding.addSymptomsBTN.setEnabled(enabled);
        binding.addMoodBTN.setEnabled(enabled);
        binding.hadFlowRB.setEnabled(enabled);
        binding.noFlowRB.setEnabled(enabled);
    }

    // Display the user selected date.
    private void setDateView() {
        binding.dateTV.setText(localDateToDateInStr(selectedDate, DATE_FULL_FORMAT));
    }

    // Display the days of selected date's month on the calendar recycler view. Here the periodDatesInMonth
    // is empty array, its data has to be read from the database.
    private void setCalendarRecyclerView() {
        binding.monthYearBTN.setText(localDateToMonthYearStr(selectedDate));
        daysOfMonth = generateDaysOfMonthArray(selectedDate);
        periodDatesInMonth = new ArrayList<>();
        recordedDatesInMonth = new ArrayList<>();

        setCalendarRecyclerViewLayout();

        setCalendarRecyclerViewAdapter();
    }

    private void setCalendarRecyclerViewLayout() {
        // Set the LayoutManager for recyclerView
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        binding.calendarRV.setLayoutManager(layoutManager);
    }

    private void setCalendarRecyclerViewAdapter() {
        // Set the selected date text color and drawable background.
        int selectedDateColor = getColor(R.color.black);
        Drawable selectedDateBackground = ResourcesCompat.getDrawable(getResources(),
                R.drawable.m_customer_circle_drawable, null);

        // Set the period dates text color and drawable background.
        int periodDatesColor = getColor(R.color.white);
        Drawable periodDatesBackground = ResourcesCompat.getDrawable(getResources(),
                R.drawable.m_customer_circle_red_drawable, null);

        // Set the recorded dates text color and drawable background.
        int recordedDatesColor = getColor(R.color.black);
        Drawable recordedDatesBackground = ResourcesCompat.getDrawable(getResources(),
                R.drawable.m_customer_circle_grey_drawable, null);

        // Set the Adapter for recyclerView.
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysOfMonth, this,
                selectedDate.getDayOfMonth(), selectedDateColor, selectedDateBackground,
                periodDatesInMonth, periodDatesColor, periodDatesBackground,
                recordedDatesInMonth, recordedDatesColor, recordedDatesBackground);
        binding.calendarRV.setAdapter(calendarAdapter);
    }

    // Read the database of selected month and find the days which has flow to form
    // `PeriodDatesInMonth`. And set a new adapter for calendar recycler view.
    private void getPeriodDatesInMonthAndUpdateCalendarRecyclerViewAdapter() {
        // Initialize periodDatesInMonth
        periodDatesInMonth = new ArrayList<>();
        recordedDatesInMonth = new ArrayList<>();
        String firstDayInMonth = generateSearchDate(1);
        String lastDayInMonth = generateSearchDate(selectedDate.lengthOfMonth());

        DatabaseReference periodRef = mDatabase.child("period");
        Query query = periodRef.orderByChild("date").startAt(firstDayInMonth).endAt(lastDayInMonth);

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null) {
                        String[] date = periodData.getDate().split("-");
                        int day = Integer.parseInt(date[2]);
                        if (periodData.getHadFlow()) {
                            periodDatesInMonth.add(day);
                        } else {
                            recordedDatesInMonth.add(day);
                        }
                    }
                }
            }
            // Update the adapter of calendar recycler view when read data is done.
            setCalendarRecyclerViewAdapter();
        });
    }

    private void setRecentPeriodView() {
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference periodRef = mDatabase.child("period");
        Query query = periodRef.orderByChild("flowAndDate").endAt("1-" + date).limitToLast(1);

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null && periodData.getHadFlow()) {
                        String startDate = periodData.getStartDate();
                        String endDate = periodData.getEndDate();

                        // Calculate the total days in the period range and update UI.
                        long totalDays = calculateDaysBetween(startDate, endDate) + 1;
                        String totalDaysInStr = String.valueOf(totalDays);

                        // Generate user friendly period range text view and display.
                        String recentPeriods = generateRecentPeriodRange(startDate, endDate);

                        String periodRangeDetails = totalDaysInStr + "-day period: " + recentPeriods;
                        binding.periodRangeDetailsTV.setText(periodRangeDetails);
                        return;
                    }
                }
                binding.periodRangeDetailsTV.setText(R.string.no_record_string);
            }
        });
    }

    // Generate user friendly period range text and display on the UI.
    private String generateRecentPeriodRange(String startDate, String endDate) {
        int startMonth = getMonthInDateShort(startDate);
        int endMonth = getMonthInDateShort(endDate);
        // Initially, start date in the format "MMMM dd", end date in the format "dd".
        String recentPeriodStart = monthValueToMonthShort(startMonth) + " " + getDayInDateShort(startDate);
        String recentPeriodEnd = String.valueOf(getDayInDateShort(endDate));
        // If start month is not equal to end month, add month label to the end date "MMMM dd".
        if (startMonth != endMonth ) {
            recentPeriodEnd = monthValueToMonthShort(endMonth) + " " + recentPeriodEnd;
        }
        // If year are not the same, add year to start date and end date. "MMMM dd year"
        int startYear = getYearInDateShort(startDate);
        int endYear = getYearInDateShort(endDate);

        if (startYear != endYear) {
            recentPeriodStart += ", " + startYear;
            recentPeriodEnd += ", " + endYear;
        }

        return recentPeriodStart + " - " + recentPeriodEnd;
    }

    private void setFlowConditionRadioGroup() {
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference flowConditionRef = mDatabase.child("period").child(date).child("hadFlow");
        flowConditionRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting flow condition data from firebase Database", task.getException());
            } else {
                Boolean value = task.getResult().getValue(Boolean.class);
                // If value is true, means this date has flow; If value is false, means this date
                // has no flow. If value is null, means there is no record.
                if(Boolean.TRUE.equals(value)) {
                    binding.periodConditionRG.check(R.id.hadFlowRB);
                } else if (Boolean.FALSE.equals(value)) {
                    binding.periodConditionRG.check(R.id.noFlowRB);
                } else {
                    binding.periodConditionRG.clearCheck();
                }
            }
        });
    }

    private void setFlowLevelView() {
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference flowLevelRef = mDatabase.child("period").child(date).child("flowLevel");
        flowLevelRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting flow level data from firebase Database", task.getException());
            } else {
                String value = task.getResult().getValue(String.class);
                if (value != null && !value.equals("")) {
                    binding.periodFlowDetailsTV.setText(value);
                } else {
                    binding.periodFlowDetailsTV.setText(R.string.no_record_string);
                }
            }
        });
    }

    private void setSymptomsView() {
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference symptomsRef = mDatabase.child("period").child(date).child("symptoms");
        symptomsRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting symptoms data from firebase Database", task.getException());
            } else {
                String value = task.getResult().getValue(String.class);
                if (value != null && !value.equals("")) {
                    binding.symptomsDetailsTV.setText(value);
                } else {
                    binding.symptomsDetailsTV.setText(R.string.no_record_string);
                }
            }
        });
    }

    private void setMoodView() {
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference moodRef = mDatabase.child("period").child(date).child("mood");
        moodRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting mood data from firebase Database", task.getException());
            } else {
                Integer value = task.getResult().getValue(Integer.class);
                String[] moodOptions = getResources().getStringArray(R.array.mood_array);
                if (value != null && value >= 0 && value < moodOptions.length) {
                    binding.moodEmojiTV.setText(moodOptions[value]);
                    binding.moodEmojiTV.setTextColor(ContextCompat.getColor(this, R.color.emoji_color));
                } else {
                    binding.moodEmojiTV.setText(R.string.no_record_string);
                    binding.moodEmojiTV.setTextColor(ContextCompat.getColor(this, R.color.black));
                }
            }
        });
    }

    private void setPeriodPredictionDateView() {
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference periodRef = mDatabase.child("period");
        Query query = periodRef.orderByChild("flowAndDate").endAt("1-" + date).limitToLast(1);

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null && periodData.getHadFlow()) {
                        LocalDate startDate = LocalDate.parse(periodData.getStartDate());
                        // Add 28 days to her last period start day
                        long defaultRange = 28;
                        int times = (int) (calculateDaysBetween(periodData.getStartDate(), date) / defaultRange + 1);
                        // Calculated PredictedDate in the format "MMM dd yyyy"
                        String predictedDate = localDateToDateInStr(startDate.plusDays(defaultRange * times), DATE_LONG_FORMAT);
                        // If the predicted date is on the same year as selected date, only display MM dd.
                        String prediction = "Your period is likely to start on: ";
                        if (Integer.parseInt(predictedDate.substring(predictedDate.length() - 4)) == selectedDate.getYear()) {
                            prediction += predictedDate.substring(0, predictedDate.length() - 4);
                        } else {
                            prediction += predictedDate;
                        }
                        binding.periodPredictionDateTV.setText(prediction);
                        return;
                    }
                }
                binding.periodPredictionDateTV.setText(R.string.period_prediction_string);
            }
        });
    }

    // Display the days of selected date's month on the calendar recycler view. Here the periodDatesInMonth
    // is empty array, its data has to be read from the database.
    private void setCycleHistoryRecyclerView() {
        cycleList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.cycleHistoryRV.setLayoutManager(linearLayoutManager);
        setCycleHistoryRecyclerViewAdapter();
    }

    private void setCycleHistoryRecyclerViewAdapter() {
        binding.cycleHistoryRV.setAdapter(new CycleAdapter(cycleList));
    }

    private void getCycleHistoryAndUpdateCycleHistoryRecyclerViewAdapter() {
        List<String> startDateList = new ArrayList<>();
        DatabaseReference periodRef = mDatabase.child("period");
        Query query = periodRef.orderByChild("startDate");

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null && periodData.getHadFlow()) {
                        String startDate = periodData.getStartDate();
                        if (!startDateList.contains(startDate)) {
                            startDateList.add(startDate);
                        }
                    }
                }
                cycleList = new ArrayList<>();
                for (int i = 0; i < startDateList.size(); i++) {
                    String startDate = startDateList.get(i);
                    String endDate; // endDate of one cycle is the day before the next period.
                    if (i == startDateList.size() - 1) {
                        endDate = localDateToDateInStr(LocalDate.now(), DATE_SHORT_FORMAT);
                    } else {
                        endDate = neighborDatesInStr(startDateList.get(i + 1), -1);
                    }
                    // Calculate the ranges between the last period start date and the next period
                    // start date.
                    int ranges = (int) calculateDaysBetween(startDate, endDate) + 1;
                    // Convert "yyyy-mm-dd" to "MMM dd" or "MMM dd yyyy"
                    String cycleStart = dateShortToLongFormat(startDate);
                    String cycleEnd = dateShortToLongFormat(endDate);
                    if (getYearInDateLong(cycleStart) == getYearInDateLong(cycleEnd)) {
                        cycleStart = cycleStart.substring(0, cycleStart.length() - 5);
                        cycleEnd = cycleEnd.substring(0, cycleEnd.length() - 5);
                    }
                    Cycle cycle = new Cycle(startDate, "Cycle: " + cycleStart + " - " + cycleEnd, ranges + " days");
                    cycleList.add(cycle);
                }
                cycleList.sort(Collections.reverseOrder());
                Log.v(TAG, cycleList.toString());
                setCycleHistoryRecyclerViewAdapter();
            }
        });
    }

    // If checked button is changed in the radio group, save changes to firebase database.
    private void registerFlowConditionRadioGroup() {
        binding.periodConditionRG.setOnCheckedChangeListener((group, checkedId) -> {
            // If checkedId is -1, no button is checked, there is no data to save.
            if (checkedId != -1) {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    if (checkedRadioButton.equals(binding.hadFlowRB)) {
                        updateDatabaseWhenHadFlowChecked();
                    } else if (checkedRadioButton.equals(binding.noFlowRB)) {
                        updateDatabaseWhenNoFlowChecked();
                    }
                }
            }
        });
    }

    private void updateDatabaseWhenHadFlowChecked() {
        DatabaseReference periodRef = mDatabase.child("period");
        // Check whether database has data or not
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        periodRef.child(date).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                PeriodData periodData = task.getResult().getValue(PeriodData.class);
                if (periodData != null) {
                    // If has data and had flow is true do nothing.
                    if (periodData.getHadFlow()) {
                        return;
                    }
                    // If has data and had flow is false, set had flow to true.
                    periodData.setHadFlow(true);
                    periodData.setStartDate(date);
                    periodData.setEndDate(date);
                } else {
                    // If no data, create new one period data for this date.
                    Long dateLong = selectedDateInMilliseconds();
                    periodData = generatePeriodDataFromDateLong(dateLong, dateLong, dateLong);
                }
                // Update date before and date after data due to the flow condition changes made to current date.
                updateDatesBeforeAndAfterDatabaseWhenHadFlowChecked(periodData);
            }
        });
    }

    private void updateDatesBeforeAndAfterDatabaseWhenHadFlowChecked(PeriodData periodDataToday) {
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        String dateBefore = neighborDatesInStr(date, -1);
        String dateAfter = neighborDatesInStr(date, 1);

        DatabaseReference periodRef = mDatabase.child("period");

        Query queryBefore = periodRef.orderByChild("endDate").equalTo(dateBefore);
        Query queryAfter = periodRef.orderByChild("startDate").equalTo(dateAfter);

        Task<DataSnapshot> task1 = queryBefore.get();
        Task<DataSnapshot> task2 = queryAfter.get();

        Tasks.whenAllSuccess(task1, task2)
                .addOnFailureListener(Throwable::printStackTrace)
                .addOnSuccessListener(list -> {
                    // Read date before and date after data from database
                    DataSnapshot dsBefore = (DataSnapshot) list.get(0); // list.get(0) store the result of task1
                    DataSnapshot dsAfter = (DataSnapshot) list.get(1);

                    List<PeriodData> periodDataBeforeList = new ArrayList<>();
                    for (DataSnapshot ds : dsBefore.getChildren()) {
                        PeriodData periodDataBefore = ds.getValue(PeriodData.class);
                        // if before had flow, update endDate
                        if (periodDataBefore != null && periodDataBefore.getHadFlow()) {
                            periodDataBefore.setEndDate(date);
                            periodDataBeforeList.add(periodDataBefore);
                        }
                    }
                    List<PeriodData> periodDataAfterList = new ArrayList<>();
                    for (DataSnapshot ds : dsAfter.getChildren()) {
                        PeriodData periodDataAfter = ds.getValue(PeriodData.class);
                        // if after had flow, download today to end date, update startDate
                        if (periodDataAfter != null && periodDataAfter.getHadFlow()) {
                            periodDataAfter.setStartDate(date);
                            periodDataAfterList.add(periodDataAfter);
                        }
                    }
                    // Save updated data to database
                    // Before and after both do not had flow. simply update today's data
                    if (periodDataBeforeList.isEmpty() && periodDataAfterList.isEmpty()) {
                        periodRef.child(date).setValue(periodDataToday).addOnSuccessListener(unused -> {
                                    Log.v(TAG, "write one period date to database is successful");
                                    updateUI();})
                                .addOnFailureListener(Throwable::printStackTrace);

                    } else if (!(periodDataBeforeList.isEmpty() || periodDataAfterList.isEmpty())) {
                        // Before and after both had flow, update all, start date is the start
                        // date of before date, end date is the end date of after date.
                        PeriodData periodDataBefore = periodDataBeforeList.get(0);
                        PeriodData periodDataAfter = periodDataAfterList.get(0);
                        // Update date before
                        for (PeriodData data : periodDataBeforeList) {
                            data.setEndDate(periodDataAfter.getEndDate());
                        }
                        // Update date after
                        for (PeriodData data : periodDataAfterList) {
                            data.setStartDate(periodDataBefore.getStartDate());
                        }
                        // Update today's data
                        periodDataToday.setStartDate(periodDataBefore.getStartDate());
                        periodDataToday.setEndDate(periodDataAfter.getEndDate());
                        // Merge two list, and add today's data to the list
                        periodDataBeforeList.addAll(periodDataAfterList);
                        periodDataBeforeList.add(periodDataToday);
                        // Save new list to database and update UI
                        saveListOfPeriodDataToDatabase(periodDataBeforeList);

                    } else if (!periodDataBeforeList.isEmpty()) {
                        // Date before had flow
                        PeriodData periodDataBefore = periodDataBeforeList.get(0);
                        periodDataToday.setStartDate(periodDataBefore.getStartDate());
                        periodDataBeforeList.add(periodDataToday);
                        saveListOfPeriodDataToDatabase(periodDataBeforeList);

                    } else { // Date after had flow
                        PeriodData periodDataAfter = periodDataAfterList.get(0);
                        periodDataToday.setEndDate(periodDataAfter.getEndDate());
                        periodDataAfterList.add(periodDataToday);
                        saveListOfPeriodDataToDatabase(periodDataAfterList);

                    }
                });
    }

    private void updateDatabaseWhenNoFlowChecked() {
        DatabaseReference periodRef = mDatabase.child("period");
        // Check whether database has data or not
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        periodRef.child(date).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                PeriodData periodData = task.getResult().getValue(PeriodData.class);
                // If no data, simply save new data to database
                if (periodData == null) {
                    periodData = new PeriodData(date, "", "", false, "", "", -1);
                    saveOnePeriodDataToDatabase(periodData);
                } else {
                    // If has data and had flow is true, for date before, update there endDate to
                    // the day before this date. For date after, change startDate or eliminate had
                    // flow condition
                    if (periodData.getHadFlow()) {
                        String startDate = periodData.getStartDate();
                        updateDatesBeforeAndAfterDatabaseWhenNoFlowChecked(startDate, date);
                    }
                }
            }
        });
    }

    private void updateDatesBeforeAndAfterDatabaseWhenNoFlowChecked(String startDate, String date) {
        DatabaseReference periodRef = mDatabase.child("period");
        Query query = periodRef.orderByChild("startDate").equalTo(startDate);

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                // Read list of periodDate from database. The data has same startDate
                List<PeriodData> periodDataList = new ArrayList<>();
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null) {
                        periodDataList.add(periodData);
                    }
                }
                // Calculate the new end date as the day before the current date.
                String newEndDate = neighborDatesInStr(date, -1);
                String newStartDate = neighborDatesInStr(date, 1);

                for (PeriodData periodData : periodDataList) {
                    int compareResult = periodData.getDate().compareTo(date);
                    if (compareResult < 0) {
                        // If the date is before the current date, update the endDate
                        periodData.setEndDate(newEndDate);
                    } else if (compareResult > 0 && periodData.getStartDate().equals(date)){
                        // If after date's start date is the current date, means user just want to
                        // eliminate the current date's data.
                        periodData.setStartDate(newStartDate);
                    } else {
                        // If the after date's startDate is not the current date, means
                        // we can end period before the current date, set its had flow to false
                        // and eliminate startDate EndDate flowLevel records.
                        periodData.setStartDate("");
                        periodData.setEndDate("");
                        periodData.setHadFlow(false);
                        periodData.setFlowLevel("");
                    }
                }
                saveListOfPeriodDataToDatabase(periodDataList);
            }
        });
    }

    private PeriodData generatePeriodDataFromDateLong(Long date, Long startDate, Long endDate) {
        String dateInStr = utcMillisecondsToDateInStr(date);
        String startDateInStr = utcMillisecondsToDateInStr(startDate);
        String endDateInStr = utcMillisecondsToDateInStr(endDate);
        return new PeriodData(dateInStr, startDateInStr, endDateInStr, true, "", "", -1);
    }

    // Save the user selected period range to database.
    private void savePeriodRangeToDatabase(Long startDate, Long endDate) {
        List<PeriodData> periodDataList = new ArrayList<>();
        Long date = startDate;
        while (date.compareTo(endDate) <= 0 ) {
            PeriodData periodData = generatePeriodDataFromDateLong(date, startDate, endDate);
            periodDataList.add(periodData);
            date = neighborDatesInMilliseconds(date, 1);
        }
        saveListOfPeriodDataToDatabase(periodDataList);
    }

    // Save a PeriodData to database.
    private void saveOnePeriodDataToDatabase(PeriodData periodData) {
        mDatabase.child("period").child(periodData.getDate()).setValue(periodData)
                .addOnSuccessListener(unused -> {
                    Log.v(TAG, "write one period date to database is successful");
                    updateUI();})
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // Save a list of PeriodData to database. Update the certain branch.
    private void saveListOfPeriodDataToDatabase(List<PeriodData> periodDataList) {
        for(PeriodData data : periodDataList) {
            Map<String, Object> periods = data.toMap();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(data.getDate(), periods);
            mDatabase.child("period").updateChildren(childUpdates)
                    .addOnSuccessListener(unused -> {
                        Log.v(TAG, "update period range data in database is successful");
                        updateUI();})
                    .addOnFailureListener(Throwable::printStackTrace);
        }
    }

    private void saveFlowLevelToDatabase(String flowLevel) {
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference periodRef = mDatabase.child("period").child(date);
        periodRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting period data from firebase Database", task.getException());
            } else {
                PeriodData periodData = task.getResult().getValue(PeriodData.class);
                if (periodData != null) {
                    // If has flow, simply update its flowLevel attribute.
                    if (periodData.getHadFlow()) {
                        periodRef.child("flowLevel").setValue(flowLevel)
                                .addOnSuccessListener(unused -> Log.v(TAG, "write one symptoms date to database is successful"))
                                .addOnFailureListener(Throwable::printStackTrace);
                        return;
                    }
                    periodData.setHadFlow(true);
                    periodData.setFlowLevel(flowLevel);
                } else {
                    periodData = new PeriodData(date, date, date, true, flowLevel, "", -1);
                }
                // Update date before and date after data due to the flow condition changes made to current date.
                updateDatesBeforeAndAfterDatabaseWhenHadFlowChecked(periodData);
            }
        });
    }

    private void saveSymptomsToDatabase(String symptoms) {
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference periodRef = mDatabase.child("period").child(date);
        periodRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting period data from firebase Database", task.getException());
            } else {
                PeriodData periodData = task.getResult().getValue(PeriodData.class);
                if (periodData != null) {
                    periodRef.child("symptoms").setValue(symptoms)
                            .addOnSuccessListener(unused -> Log.v(TAG, "write one symptoms date to database is successful"))
                            .addOnFailureListener(Throwable::printStackTrace);
                } else {
                    periodData = new PeriodData(date, "", "", false, "", symptoms, -1);
                    saveOnePeriodDataToDatabase(periodData);
                }
            }
        });
    }

    private void saveMoodToDatabase(int mood) {
        String date = localDateToDateInStr(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference periodRef = mDatabase.child("period").child(date);
        periodRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting period data from firebase Database", task.getException());
            } else {
                PeriodData periodData = task.getResult().getValue(PeriodData.class);
                if (periodData != null) {
                    periodRef.child("mood").setValue(mood)
                            .addOnSuccessListener(unused -> Log.v(TAG, "write mood date to database is successful"))
                            .addOnFailureListener(Throwable::printStackTrace);
                } else {

                    periodData = new PeriodData(date,"", "", false, "", "", mood);
                    saveOnePeriodDataToDatabase(periodData);
                }
            }
        });
    }

    // Search date is used to narrow down the search range when performing query on the firebase realtime database.
    private String generateSearchDate(int day) {
        String year = String.format(Locale.US,"%02d", selectedDate.getYear());
        String monthValue = String.format(Locale.US,"%02d", selectedDate.getMonthValue());
        String date = String.format(Locale.US, "%02d", day);

        return year + "-" + monthValue + "-" + date;
    }

    // Generate the days of month array according to the month of user selected date. The array is
    // used as the item list in calendar recycler view.
    private List<String> generateDaysOfMonthArray(LocalDate date) {
        List<String> daysOfMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 1; i <= 42; i ++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysOfMonthArray.add("");
            } else {
                daysOfMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysOfMonthArray;
    }

    private String dateShortToLongFormat(String date) {
        String[] dateSplit = date.split("-");
        String year = dateSplit[0];
        String month = monthValueToMonthShort(Integer.parseInt(dateSplit[1]));
        String day = String.valueOf(Integer.parseInt(dateSplit[2]));
        return month + " " + day + " " + year;
    }

    // Convert LocalDate to date in specified string format.
    private String localDateToDateInStr(LocalDate date, String dateFormat) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
        return date.format(dateTimeFormatter);
    }

    // Convert LocalDate to MONTH_YEAR_FORMAT string.
    private String localDateToMonthYearStr(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT);
        return date.format(dateTimeFormatter);
    }

    // Convert milliseconds in UTC time to date in DATE_SHORT_FORMAT string.
    private String utcMillisecondsToDateInStr(Long milliseconds) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(milliseconds);
        SimpleDateFormat format = new SimpleDateFormat(PeriodActivity.DATE_SHORT_FORMAT, Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(calendar.getTime());
    }

    // Get the milliseconds of selectedDate. Zone is set as system default.
    private Long selectedDateInMilliseconds() {
        return selectedDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // Get the date before and date after in milliseconds. `days` > 0 means days after today.
    // `days` < 0 means the days before today. `today` is in milliseconds format.
    private Long neighborDatesInMilliseconds(Long today, int days) {
        return today + (long) days * DAY_TO_MILLISECONDS;
    }

    // Get the date before and date after in DATE_SHORT_FORMAT String . `days` > 0 means days after today.
    // `days` < 0 means the days before today. `today` is in DATE_SHORT_FORMAT.
    private String neighborDatesInStr(String today, int days) {
        return localDateToDateInStr(LocalDate.parse(today).plusDays(days), DATE_SHORT_FORMAT);
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

    // Convert month value to short month string.
    private String monthValueToMonthShort(int month) {
        return new DateFormatSymbols().getShortMonths()[month - 1];
    }

    private int getYearInDateLong(String date) {
        return Integer.parseInt(date.split(" ")[2]);
    }

    private int getYearInDateShort(String date) {
        return Integer.parseInt(date.split("-")[0]);
    }

    private int getMonthInDateShort(String date) {
        return Integer.parseInt(date.split("-")[1]);
    }

    private int getDayInDateShort(String date) {
        return Integer.parseInt(date.split("-")[2]);
    }

    // Set the bottom navigation view. Display the selected home.
    private void setBottomNavigationView() {
        // Initialize and assign variable
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

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
