package com.example.stayhealthy_android_app.Period;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.util.Pair;
import androidx.emoji2.widget.EmojiTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.AwardActivity;
import com.example.stayhealthy_android_app.JourneyActivity;
import com.example.stayhealthy_android_app.Period.Calendar.CalendarAdapter;
import com.example.stayhealthy_android_app.Period.Model.PeriodData;
import com.example.stayhealthy_android_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PeriodActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{
    private final static String TAG = "MPeriodActivity";
    private final static String DATE_FULL_FORMAT = "EEEE, MMMM d, yyyy";
    private final static String MONTH_YEAR_FORMAT = "MMMM yyyy";
    private final static String DATE_LONG_FORMAT = "MMMM dd yyyy";
    private final static String DATE_SHORT_FORMAT = "yyyy-MM-dd";
    private final static String SELECT_DATE_KEY = "select_key";
    private final static int DAY_TO_MILLISECONDS = 86400000; // that is: 24 * 60 * 60 * 1000
    private DatabaseReference mDatabase;
    private BottomNavigationView bottomNavigationView;
    private MaterialButton monthYearBTN;
    private RecyclerView calendarRV;
    private LocalDate selectedDate;
    private TextView dateTV;
    private TextView periodDateDetailsTV;
    private TextView periodFlowDetailsTV;
    private TextView symptomsDetailsTV;
    private EmojiTextView moodEmojiTV;
    private TextView periodCurrentCycleTV;
    private TextView periodCycleTotalDaysTV;
    private TextView periodPredictionDateTV;
    private RadioGroup periodConditionRG;
    private RadioButton hadFlowRB;
    private RadioButton noFlowRB;
    private List<String> daysOfMonth;
    private List<Integer> periodDatesInMonth;
    private int hadFlowFlag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period);

        // Get the current user from firebase authentication.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Set up the firebase Database reference.
        assert user != null;
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        // Initialize the selected date as today.
        selectedDate = LocalDate.now();

        // Initialize the days of month array in selectedDate's month.
        daysOfMonth = new ArrayList<>();

        // Initialize the recorded period dates in selectedDate's month.
        periodDatesInMonth = new ArrayList<>();

        hadFlowFlag = -1;

        // Initialize the widgets on the screen.
        initWidgets();

        // Set bottom navigation view.
        setBottomNavigationView();
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

    // Call this method every time the selected date is changed. The information displayed on the
    // screen is based on the selected date.
    private void updateUI() {
        // Set the selected date view. no need to read data.
        setDateView();
        // Set the initial calendar recycler view which displays the days of the selected month.
        setCalendarRecyclerView();
        // Read this month's period dates from database and update the adapter for calendarRV.
        generatePeriodDatesInMonthAndUpdateCalendarRecyclerViewAdapter();

        // Read `hadFlow`, `flowLevel`, `symptoms` and `mood` data from database and update UI.
        setFlowConditionRadioGroup();
        setFlowLevelView();
        setSymptomsView();
        setMoodView();
    }

    // If checked button is changed in the radio group, save changes to firebase database.
    private void registerFlowConditionRadioGroup() {
        periodConditionRG.setOnCheckedChangeListener((group, checkedId) -> {
            // If checkedId is -1, no button is checked, there is no data to save.
            if (checkedId != -1) {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    // TODO: This date has flow, check the database. If the day behind has flow, then that
                    // date's startDate is today's startDate. If the day behind does not have flow, then
                    // today is the startDate, have to check the days after today, if has flow, update
                    // those days startDate.
                    if (hadFlowRB.isChecked()) {
                        updateDatabaseHadFlowChecked();
                    } else {
                        updateDatabaseNoFlowChecked();
                    }
                }
            }
        });
    }

    private void updateDatabaseHadFlowChecked() {
        List<PeriodData> periodDataList = new ArrayList<>();
        Long date = selectedDateInMilliseconds();
        // Search range, 1 days before today to 1 days after today.
        String startDate = convertUtcMillisecondsToDate(neighborDaysInMilliseconds(date, -1), DATE_SHORT_FORMAT);
        String dateInStr = convertUtcMillisecondsToDate(date, DATE_SHORT_FORMAT);
        String endDate = convertUtcMillisecondsToDate(neighborDaysInMilliseconds(date, 7), DATE_SHORT_FORMAT);

        DatabaseReference periodRef = mDatabase.child("period");
        Query periodMonthQuery = periodRef.orderByChild("date").startAt(startDate).endAt(endDate);

        periodMonthQuery.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                // Indicates whether the date is already stored in the database or not.
                boolean dateInDatabase = false;
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null) {
                        periodDataList.add(periodData);
                        if (periodData.getDate().equals(dateInStr)) {
                            dateInDatabase = true;
                        }
                    }
                }
                if (periodDataList.isEmpty()) {
                    saveOnePeriodDateInStrToDatabase(dateInStr, dateInStr, true, "", "", -1);
                    return;
                }

                for(PeriodData periodData : periodDataList) {
                    if (periodData.getDate().equals(startDate) && periodData.getHadFlow()) {
                        if (dateInDatabase) {
                            periodRef.child(dateInStr).child("hadFlow").setValue(true)
                                    .addOnSuccessListener(unused -> Log.v(TAG, "write flow condition to database is successful"))
                                    .addOnFailureListener(Throwable::printStackTrace);
                            periodRef.child(dateInStr).child("startDate").setValue(periodData.getStartDate())
                                    .addOnSuccessListener(unused -> Log.v(TAG, "write start date to database is successful"))
                                    .addOnFailureListener(Throwable::printStackTrace);
                        } else {
                            saveOnePeriodDateInStrToDatabase(dateInStr, periodData.getStartDate(), true, "", "", -1);
                        }
                        break;
                    }
                    if (periodData.getDate().equals(dateInStr)) {
                        periodRef.child(dateInStr).child("hadFlow").setValue(true)
                                .addOnSuccessListener(unused -> Log.v(TAG, "write flow condition to database is successful"))
                                .addOnFailureListener(Throwable::printStackTrace);
                    }
                    if (periodData.getHadFlow()) {
                        if (periodData.getDate().equals(startDate)) {
                            saveOnePeriodDateInStrToDatabase(dateInStr, periodData.getStartDate(), true, "", "", -1);
                        }
                        if (periodData.getDate().compareTo(dateInStr) < 0) {
                            periodRef.child(dateInStr).child("hadFlow").setValue(true);
                        }
                    }
                }
            }
        });
    }

    private void updateDatabaseNoFlowChecked() {

    }

    // addPeriodDateBTN onClickListener. A datePicker shown when clicked. User can choose a date or
    // a range of dates through this picker.
    public void addPeriodDate(View view) {
        // Set default selection range as 2 days ago to today. StartDate is 4 days ago before the
        // end date. TODO: This number of days ago can be set as the specific user period range. Typical period lasts for 5 days
        int daysAgo = -4;
        Long defaultEndDateInMilliseconds = selectedDateInMilliseconds();
        Long defaultStartDateInMilliseconds = neighborDaysInMilliseconds(defaultEndDateInMilliseconds, daysAgo);
        final MaterialDatePicker<Pair<Long, Long>> materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Period Range")
                .setSelection(new Pair<>(defaultStartDateInMilliseconds, defaultEndDateInMilliseconds))
                .build();
        materialDatePicker.show(getSupportFragmentManager(), "PeriodDateRangePicker");

        materialDatePicker.addOnPositiveButtonClickListener(
                (MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>) selection
                        -> {
                    Long startDateInMilliseconds = selection.first;
                    Long endDateInMilliseconds = selection.second;
                    String startDate = convertUtcMillisecondsToDate(startDateInMilliseconds, DATE_SHORT_FORMAT);
                    String[] startDateSplit = startDate.split("-");
                    int year = Integer.parseInt(startDateSplit[0]);
                    int month = Integer.parseInt(startDateSplit[1]);
                    int day = Integer.parseInt(startDateSplit[2]);
                    setSelectedDate(year, month, day);
                    savePeriodRangeToDatabase(startDateInMilliseconds, endDateInMilliseconds);
                });

    }

    // Convert milliseconds in UTC time to date in string
    private String convertUtcMillisecondsToDate(Long milliseconds, String dateFormat) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(milliseconds);
        SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(calendar.getTime());
    }

    private void saveOnePeriodDateInStrToDatabase(String date, String startDate, boolean hadFlow,
                                                  String flowLevel, String symptoms, int mood) {
        PeriodData periodData = new PeriodData(date, startDate, hadFlow, flowLevel, symptoms, mood);
        mDatabase.child("period").child(date).setValue(periodData)
                .addOnSuccessListener(unused -> {
                    Log.v(TAG, "write one period date to database is successful");
                    updateUI();
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void saveOnePeriodDateInLongToDatabase(Long date, Long startDate, boolean hadFlow,
                                                   String flowLevel, String symptoms, int mood) {
        String dateInStr = convertUtcMillisecondsToDate(date, DATE_SHORT_FORMAT);
        String startDateInStr = convertUtcMillisecondsToDate(startDate, DATE_SHORT_FORMAT);
        saveOnePeriodDateInStrToDatabase(dateInStr, startDateInStr, hadFlow, flowLevel, symptoms, mood);
    }

    private void savePeriodRangeToDatabase(Long startDate, Long endDate) {
        Long date = startDate;
        saveOnePeriodDateInLongToDatabase(date, startDate, true, "", "", -1);
        while (!date.equals(endDate)) {
            date = neighborDaysInMilliseconds(date, 1);
            saveOnePeriodDateInLongToDatabase(date, startDate, true, "", "", -1);
        }
    }

    // addPeriodFlowBTN onClickListener. An alert dialog is shown where user can choose a flow level.
    public void addFlowLevel(View view) {
        String[] flowLevelOptions = getResources().getStringArray(R.array.flow_level_array);
        Integer[] checkedFlowLevel = {0};
        for (int i = 0; i < flowLevelOptions.length; i++) {
            if (periodFlowDetailsTV.getText().equals(flowLevelOptions[i])) {
                checkedFlowLevel[0] = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.choose_flow_level_string)
                .setSingleChoiceItems(flowLevelOptions, checkedFlowLevel[0], (dialog, which)
                        -> checkedFlowLevel[0] = which)
                .setPositiveButton(R.string.ok_string, (dialog, which)
                        -> {
                    String flowLevel = flowLevelOptions[checkedFlowLevel[0]];
                    periodFlowDetailsTV.setText(flowLevel);
                    saveFlowLevelToDatabase(flowLevel);
                })
                .setNegativeButton(R.string.cancel_string, null)
                .create()
                .show();
    }

    private void saveFlowLevelToDatabase(String flowLevel) {
        DatabaseReference periodRef = mDatabase.child("period").child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT));
        periodRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting period data from firebase Database", task.getException());
            } else {
                PeriodData value = task.getResult().getValue(PeriodData.class);
                if (value != null) {
                    periodRef.child("flowLevel").setValue(flowLevel)
                            .addOnSuccessListener(unused -> Log.v(TAG, "write flow level date to database is successful"))
                            .addOnFailureListener(Throwable::printStackTrace);
                } else {
                    Long date = selectedDateInMilliseconds();
                    saveOnePeriodDateInLongToDatabase(date, date, true, flowLevel, "", -1);
                }
            }
        });
    }

    // addSymptomsBTN onClickListener. An alert dialog is shown where user can choose one or more
    // symptoms.
    public void addSymptoms(View view) {
        String[] symptomsOptions = getResources().getStringArray(R.array.symptoms_array);
        boolean[] checkedSymptoms = new boolean[symptomsOptions.length];
        ArrayList<String> symptomsList = new ArrayList<>(Arrays.asList(symptomsDetailsTV.getText().toString().split(", ")));
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
                    symptomsDetailsTV.setText(stringBuilder);
                    saveSymptomsToDatabase(stringBuilder.toString());
                })
                .setNegativeButton(R.string.cancel_string, null)
                .create()
                .show();
    }

    private void saveSymptomsToDatabase(String symptoms) {
        DatabaseReference periodRef = mDatabase.child("period").child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT));
        periodRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting period data from firebase Database", task.getException());
            } else {
                PeriodData value = task.getResult().getValue(PeriodData.class);
                if (value != null) {
                    periodRef.child("symptoms").setValue(symptoms)
                            .addOnSuccessListener(unused -> Log.v(TAG, "write one symptoms date to database is successful"))
                            .addOnFailureListener(Throwable::printStackTrace);
                } else {
                    Long date = selectedDateInMilliseconds();
                    saveOnePeriodDateInLongToDatabase(date, date, true, "", symptoms, -1);
                }
            }
        });
    }

    // addMood onClickListener. An alert dialog is shown where user can choose a mood.
    public void addMood(View view) {
        // Create mood options string.
        String[] moodOptions = getResources().getStringArray(R.array.mood_array);
        // Set default checked item position as the index 0.
        Integer[] checkedMood = {0};
        // Set the checked item as the one shown on the moodEmojiTV.
        for (int i = 0; i < moodOptions.length; i++) {
            if (moodEmojiTV.getText().equals(moodOptions[i])) {
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
                    moodEmojiTV.setText(moodOptions[checkedMood[0]]);
                    moodEmojiTV.setTextColor(ContextCompat.getColor(this, R.color.emoji_color));
                    saveMoodToDatabase(checkedMood[0]);})
                .setNegativeButton(R.string.cancel_string, null)
                .setView(gridView)
                .create()
                .show();
    }

    private void saveMoodToDatabase(int mood) {
        DatabaseReference periodRef = mDatabase.child("period").child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT));
        periodRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting period data from firebase Database", task.getException());
            } else {
                PeriodData value = task.getResult().getValue(PeriodData.class);
                if (value != null) {
                    periodRef.child("mood").setValue(mood)
                            .addOnSuccessListener(unused -> Log.v(TAG, "write mood date to database is successful"))
                            .addOnFailureListener(Throwable::printStackTrace);
                } else {
                    Long date = selectedDateInMilliseconds();
                    saveOnePeriodDateInLongToDatabase(date, date, true, "", "", mood);
                }
            }
        });
    }

    // previousBTN onClickListener
    public void previousMonthAction(View view) {
        selectedDate = selectedDate.minusMonths(1);
        updateUI();
    }

    // nextBTN onClickListener
    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1);
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

    // Initialize widgets on the period activity screen.
    private void initWidgets() {
        monthYearBTN = (MaterialButton) findViewById(R.id.monthYearBTN);
        calendarRV = (RecyclerView) findViewById(R.id.calendarRV);
        dateTV = (TextView) findViewById(R.id.dateTV);
        periodDateDetailsTV = (TextView) findViewById(R.id.periodDateDetailsTV);
        periodFlowDetailsTV = (TextView) findViewById(R.id.periodFlowDetailsTV);
        symptomsDetailsTV = (TextView) findViewById((R.id.symptomsDetailsTV));
        moodEmojiTV = (EmojiTextView) findViewById(R.id.moodEmojiTV);
        periodCurrentCycleTV = (TextView) findViewById(R.id.periodCurrentCycleTV);
        periodCycleTotalDaysTV = (TextView) findViewById(R.id.periodCycleTotalDaysTV);
        periodPredictionDateTV = (TextView) findViewById(R.id.periodPredictionTV);
        periodConditionRG = (RadioGroup) findViewById(R.id.periodConditionRG);
        hadFlowRB = (RadioButton) findViewById(R.id.hadFlowRB);
        noFlowRB = (RadioButton) findViewById(R.id.noFlowRB);
    }

    private void setFlowConditionRadioGroup() {
        DatabaseReference flowConditionRef = mDatabase.child("period")
                .child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT)).child("hadFlow");
        flowConditionRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting flow condition data from firebase Database", task.getException());
            } else {
                Boolean value = task.getResult().getValue(Boolean.class);
                // If value is true, means this date has flow; If value is false, means this date
                // has no flow. If value is null, means there is no record.
                if(Boolean.TRUE.equals(value)) {
                    periodConditionRG.check(R.id.hadFlowRB);
                    hadFlowFlag = 1;
                } else if (Boolean.FALSE.equals(value)) {
                    periodConditionRG.check(R.id.noFlowRB);
                    hadFlowFlag = 0;
                } else {
                    periodConditionRG.clearCheck();
                    hadFlowFlag = -1;
                }
            }
        });
    }

    private void setFlowLevelView() {
        DatabaseReference flowLevelRef = mDatabase.child("period")
                .child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT)).child("flowLevel");
        flowLevelRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting flow level data from firebase Database", task.getException());
            } else {
                String value = task.getResult().getValue(String.class);
                if (value != null && !value.equals("")) {
                    periodFlowDetailsTV.setText(value);
                } else {
                    periodFlowDetailsTV.setText(R.string.no_record_string);
                }
            }
        });
    }

    private void setSymptomsView() {
        DatabaseReference symptomsRef = mDatabase.child("period")
                .child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT)).child("symptoms");
        symptomsRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting symptoms data from firebase Database", task.getException());
            } else {
                String value = task.getResult().getValue(String.class);
                if (value != null && !value.equals("")) {
                    symptomsDetailsTV.setText(value);
                } else {
                    symptomsDetailsTV.setText(R.string.no_record_string);
                }
            }
        });
    }

    private void setMoodView() {
        DatabaseReference moodRef = mDatabase.child("period")
                .child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT)).child("mood");
        moodRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting mood data from firebase Database", task.getException());
            } else {
                Integer value = task.getResult().getValue(Integer.class);
                String[] moodOptions = getResources().getStringArray(R.array.mood_array);
                if (value != null && value >= 0 && value < moodOptions.length) {
                    moodEmojiTV.setText(moodOptions[value]);
                    moodEmojiTV.setTextColor(ContextCompat.getColor(this, R.color.emoji_color));
                } else {
                    moodEmojiTV.setText(R.string.no_record_string);
                    moodEmojiTV.setTextColor(ContextCompat.getColor(this, R.color.black));
                }
            }
        });
    }

    // Display the user selected date.
    private void setDateView() {
        dateTV.setText(convertLocalDateToStringDate(selectedDate, DATE_FULL_FORMAT));
    }

    // Display the days of selected date's month on the calendar recycler view. Here the periodDatesInMonth
    // is empty array, its data has to be read from the database.
    private void setCalendarRecyclerView() {
        monthYearBTN.setText(monthYearFromDate(selectedDate));
        daysOfMonth = generateDaysOfMonthArray(selectedDate);
        periodDatesInMonth = new ArrayList<>();

        setCalendarRecyclerViewLayout();

        setCalendarRecyclerViewAdapter();
    }

    private void setCalendarRecyclerViewLayout() {
        // Set the LayoutManager for recyclerView
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRV.setLayoutManager(layoutManager);
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

        // Set the Adapter for recyclerView, here the periodDatesInMonth is empty
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysOfMonth, this,
                selectedDate.getDayOfMonth(), selectedDateColor, selectedDateBackground, periodDatesInMonth, periodDatesColor, periodDatesBackground);
        calendarRV.setAdapter(calendarAdapter);
    }

    private String calculateSearchDate(int day) {
        String year = String.format(Locale.US,"%02d", selectedDate.getYear());
        String monthValue = String.format(Locale.US,"%02d", selectedDate.getMonthValue());
        String date = String.format(Locale.US, "%02d", day);

        return year + "-" + monthValue + "-" + date;
    }

    // Read the database of selected month and find the days which has flow to form
    // `PeriodDatesInMonth`. And set a new adapter for calendar recycler view.
    private void generatePeriodDatesInMonthAndUpdateCalendarRecyclerViewAdapter() {
        // Initialize periodDatesInMonth
        periodDatesInMonth = new ArrayList<>();
        String firstDayInMonth = calculateSearchDate(1);
        String lastDayInMonth = calculateSearchDate(selectedDate.lengthOfMonth());

        DatabaseReference periodRef = mDatabase.child("period");
        Query periodMonthQuery = periodRef.orderByChild("date").startAt(firstDayInMonth).endAt(lastDayInMonth);

        periodMonthQuery.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null) {
                        if (periodData.getHadFlow()) {
                            String[] date = periodData.getDate().split("-");
                            int day = Integer.parseInt(date[2]);
                            periodDatesInMonth.add(day);
                        }
                    }
                }
            }
            // Update the adapter of calendar recycler view when read data is done.
            setCalendarRecyclerViewAdapter();
        });
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

    // Convert LocalDate to date in string.
    private String convertLocalDateToStringDate(LocalDate date, String dateFormat) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
        return date.format(dateTimeFormatter);
    }

    // Get month year string from LocalDate
    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT);
        return date.format(dateTimeFormatter);
    }

    // Update user selected date. Convert the (year, month, day) to LocalDate format.
    private void setSelectedDate(int year, int month, int day) {
        selectedDate = selectedDate.plusDays(day - selectedDate.getDayOfMonth());
        selectedDate = selectedDate.plusMonths(month - (selectedDate.getMonthValue()));
        selectedDate = selectedDate.plusYears(year - selectedDate.getYear());
    }

    // Get the milliseconds of selectedDate. Zone is set as system default.
    private Long selectedDateInMilliseconds() {
        return selectedDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // Get number of neighbor days in milliseconds. `days` > 0 means days after today.
    // `days` < 0 means the days before today. `today` is in milliseconds format.
    private Long neighborDaysInMilliseconds(Long today, int days) {
        return today + (long) days * DAY_TO_MILLISECONDS;
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

    private int monthValueFromMonthName(String name) {
        return Month.valueOf(name.toUpperCase()).getValue();
    }

    private void setRecentPeriodView() {

    }

    private void setCycleHistoryRangeView() {

    }
    private void setTotalCycleDaysView() {

    }

    private void SetPeriodPredictionDate() {

    }
}
