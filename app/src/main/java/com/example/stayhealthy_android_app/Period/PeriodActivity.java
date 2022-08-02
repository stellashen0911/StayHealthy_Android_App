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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.AwardActivity;
import com.example.stayhealthy_android_app.JourneyActivity;
import com.example.stayhealthy_android_app.Period.Calendar.CalendarAdapter;
import com.example.stayhealthy_android_app.Period.Model.PeriodData;
import com.example.stayhealthy_android_app.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.example.stayhealthy_android_app.databinding.ActivityPeriodBinding;

public class PeriodActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{
    private final static String TAG = "MPeriodActivity";
    private final static String DATE_FULL_FORMAT = "EEEE, MMMM d, yyyy";
    private final static String MONTH_YEAR_FORMAT = "MMMM yyyy";
    private final static String DATE_LONG_FORMAT = "MMMM dd yyyy";
    private final static String DATE_SHORT_FORMAT = "yyyy-MM-dd";
    private final static String SELECT_DATE_KEY = "select_key";
    private final static int DAY_TO_MILLISECONDS = 86400000; // that is: 24 * 60 * 60 * 1000
    private ActivityPeriodBinding binding;
    private DatabaseReference mDatabase;
    private BottomNavigationView bottomNavigationView;
    private LocalDate selectedDate;
    private List<String> daysOfMonth;
    private List<Integer> periodDatesInMonth;

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

        // Initialize the days of month array in selectedDate's month.
        daysOfMonth = new ArrayList<>();

        // Initialize the recorded period dates in selectedDate's month.
        periodDatesInMonth = new ArrayList<>();

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
        setRecentPeriodView();
        setFlowConditionRadioGroup();
        setFlowLevelView();
        setSymptomsView();
        setMoodView();
        setPeriodPredictionDate();
    }

    public void expandCycleHistory(View view) {
        Intent intent = new Intent(this, CycleHistoryActivity.class);
        startActivity(intent);
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

    private void updateDatesBeforeAndAfterInDatabase(PeriodData periodDataToday) {
        String selectedDateInStr = convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT);
        String dateBefore = convertLocalDateToStringDate(LocalDate.parse(selectedDateInStr).minusDays(1), DATE_SHORT_FORMAT);
        String dateAfter = convertLocalDateToStringDate(LocalDate.parse(selectedDateInStr).plusDays(1), DATE_SHORT_FORMAT);

        Log.v(TAG, dateBefore + " " + dateAfter);

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
                            periodDataBefore.setEndDate(selectedDateInStr);
                            periodDataBeforeList.add(periodDataBefore);
                        }
                    }
                    List<PeriodData> periodDataAfterList = new ArrayList<>();
                    for (DataSnapshot ds : dsAfter.getChildren()) {
                        PeriodData periodDataAfter = ds.getValue(PeriodData.class);
                        // if after had flow, download today to end date, update startDate
                        if (periodDataAfter != null && periodDataAfter.getHadFlow()) {
                            periodDataAfter.setStartDate(selectedDateInStr);
                            periodDataAfterList.add(periodDataAfter);
                        }
                    }
                    // Save updated data to database
                    // Before and after both do not had flow. simply update today's data
                    if (periodDataBeforeList.isEmpty() && periodDataAfterList.isEmpty()) {
                        periodRef.child(selectedDateInStr).setValue(periodDataToday).addOnSuccessListener(unused -> {
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

    private void updateDatabaseWhenHadFlowChecked() {
        DatabaseReference periodRef = mDatabase.child("period");
        // Check whether database has data or not
        String date = convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT);
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
                } else {
                    // If no data, create new one period data for this date.
                    Long dateLong = selectedDateInMilliseconds();
                    periodData = generatePeriodDataFromDateLong(dateLong, dateLong, dateLong,true, "", "", -1);
                }
                // Update date before and date after data due to the flow condition changes made to current date.
                updateDatesBeforeAndAfterInDatabase(periodData);
            }
        });
    }

    private void updateDatabaseWhenNoFlowChecked() {
        DatabaseReference periodRef = mDatabase.child("period");
        // Check whether database has data or not
        String date = convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT);
        periodRef.child(date).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                PeriodData periodData = task.getResult().getValue(PeriodData.class);
                //



                // if has data and had flow is true do nothing.
                // if has data and had flow is false or no data, check the date before and after
                // selected date.
                if (periodData != null) {
                    if (!periodData.getHadFlow()) {
                        // if has data, update the previous PeriodData
                        updateDatesBeforeAndAfterInDatabase(periodData);
                        Log.v(TAG, "has data and had flow is false");
                    }
                } else {
                    Log.v(TAG, "no data");
                    // if no data, create new one period data
                    Long dateLong = selectedDateInMilliseconds();
                    periodData = generatePeriodDataFromDateLong(dateLong, dateLong, dateLong,true, "", "", -1);
                    updateDatesBeforeAndAfterInDatabase(periodData);
                }
            }
        });
    }

    private void updateSelectedInDateDatabase() {
        String selectedDateInStr = convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference periodRef = mDatabase.child("period");

        periodRef.child(selectedDateInStr).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                PeriodData periodData = task.getResult().getValue(PeriodData.class);
                if (periodData != null) {
                    DatabaseReference toPath = periodRef.child("period").child("noFlow").child(selectedDateInStr);
                    moveRecord(periodRef.child(selectedDateInStr), toPath);
                }
            }
        });

    }

    private void updatePreviousDatabase() {
        String selectedDateInStr = convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference periodRef = mDatabase.child("period");
        Query periodMonthQuery = periodRef.orderByChild("date").endBefore(selectedDateInStr).limitToFirst(1);

        periodMonthQuery.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null) {
                        String endDate = periodData.getDate().equals(selectedDateInStr) ? selectedDateInStr : periodData.getStartDate();

                        return;
                    }
                }
            }
        });
    }

    // addPeriodDateBTN onClickListener. A datePicker shown when clicked. User can choose a date or
    // a range of dates through this picker.
    public void addPeriodDate(View view) {
        // Set default selection range as 2 days ago to today. StartDate is 4 days ago before the
        // end date.
        int daysAgo = -4;
        Long defaultEndDateInMilliseconds = selectedDateInMilliseconds();
        Long defaultStartDateInMilliseconds = neighborDaysInMilliseconds(defaultEndDateInMilliseconds, daysAgo);
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
                    String startDate = convertUtcMillisecondsToDate(startDateInMilliseconds, DATE_SHORT_FORMAT);
                    selectedDate = LocalDate.parse(startDate);
                });
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

    // Save a PeriodData to database.
    private void saveOnePeriodDateToDatabase(PeriodData periodData) {
        mDatabase.child("period").child(periodData.getDate()).setValue(periodData)
                .addOnSuccessListener(unused -> {
                    Log.v(TAG, "write one period date to database is successful");
                    updateUI();})
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private PeriodData generatePeriodDataFromDateLong(Long date, Long startDate, Long endDate, Boolean hadFlow, String flowLevel, String symptoms, int mood) {
        String dateInStr = convertUtcMillisecondsToDate(date, DATE_SHORT_FORMAT);
        String startDateInStr = convertUtcMillisecondsToDate(startDate, DATE_SHORT_FORMAT);
        String endDateInStr = convertUtcMillisecondsToDate(endDate, DATE_SHORT_FORMAT);
        return new PeriodData(dateInStr, startDateInStr, endDateInStr, hadFlow, flowLevel, symptoms, mood);
    }

    // Save multiple period data to database at once.
    private void savePeriodRangeToDatabase(Long startDate, Long endDate) {
        Long date = startDate;
        PeriodData periodData = generatePeriodDataFromDateLong(date, startDate, endDate, true, "", "", -1);
        Map<String, Object> periods = periodData.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(periodData.getDate(), periods);
        while (!date.equals(endDate)) {
            date = neighborDaysInMilliseconds(date, 1);
            periodData = generatePeriodDataFromDateLong(date, startDate, endDate, true, "", "", -1);
            periods = periodData.toMap();
            childUpdates.put(periodData.getDate(), periods);
        }
        mDatabase.child("period").updateChildren(childUpdates)
                .addOnSuccessListener(unused -> {
                    Log.v(TAG, "write period range date to database is successful");
                    updateUI();})
                .addOnFailureListener(Throwable::printStackTrace);
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

    private void saveFlowLevelToDatabase(String flowLevel) {
        DatabaseReference periodRef = mDatabase.child("period").child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT));
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
                    String date = convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT);
                    periodData = new PeriodData(date, date, date, true, flowLevel, "", -1);
                }
                // Update date before and date after data due to the flow condition changes made to current date.
                updateDatesBeforeAndAfterInDatabase(periodData);
            }
        });
    }

    // addSymptomsBTN onClickListener. An alert dialog is shown where user can choose one or more
    // symptoms.
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

    private void saveSymptomsToDatabase(String symptoms) {
        DatabaseReference periodRef = mDatabase.child("period").child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT));
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
                    String selectedDateInStr = convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT);
                    periodData = new PeriodData(selectedDateInStr, "", "", false, "", symptoms, -1);
                    saveOnePeriodDateToDatabase(periodData);
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

    private void saveMoodToDatabase(int mood) {
        DatabaseReference periodRef = mDatabase.child("period").child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT));
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
                    String selectedDateInStr = convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT);
                    periodData = new PeriodData(selectedDateInStr,"", "", false, "", "", mood);
                    saveOnePeriodDateToDatabase(periodData);
                }
            }
        });
    }

    // previousBTN onClickListener
    public void previousMonthAction(View view) {
        if (selectedDate.getDayOfMonth() == 1) {
            selectedDate = selectedDate.minusMonths(1);
            int lastDay = selectedDate.lengthOfMonth();
            selectedDate = selectedDate.withDayOfMonth(lastDay);
        } else {
            selectedDate = selectedDate.minusMonths(1);
        }
        updateUI();
    }

    // nextBTN onClickListener
    public void nextMonthAction(View view) {
        // If currently on the last day of the month ,we go to first day of next month.
        if (selectedDate.getDayOfMonth() == selectedDate.lengthOfMonth()) {
            selectedDate = selectedDate.plusMonths(1).withDayOfMonth(1);
        } else {
            selectedDate = selectedDate.plusMonths(1);
        }
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

    private void setRecentPeriodView() {
        String selectedDateInStr = convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference periodRef = mDatabase.child("period");
        Query periodQuery = periodRef.orderByChild("flowAndDate").endAt("1-" + selectedDateInStr).limitToLast(1);

        periodQuery.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null && periodData.getHadFlow()) {
                        String startDate = dateShortToLongFormat(periodData.getStartDate());
                        String endDate = dateShortToLongFormat(periodData.getEndDate());
                        String startDateSub = startDate.substring(0, startDate.length() - 4);
                        String recentPeriods;
                        if (startDate.equals(endDate)) {
                            recentPeriods = startDateSub;
                        } else if (startDate.substring(startDate.length() - 4).equals(endDate.substring(endDate.length() - 4))) {
                            recentPeriods = startDateSub + " - " + endDate.substring(0, endDate.length() - 4);
                        } else {
                            recentPeriods = startDate + " - " + endDate;
                        }
                        binding.periodDateDetailsTV.setText(recentPeriods);

                        // Calculate the total days in the period range.
                        String totalDays = calculatePeriodRangeTotalDays(periodData.getStartDate(),
                                periodData.getEndDate()) + " days";
                        binding.periodRangeTotalDaysTV.setText(totalDays);
                        return;
                    }
                }
                binding.periodDateDetailsTV.setText(R.string.no_record_string);
                binding.periodRangeTotalDaysTV.setText(R.string.no_record_string);
            }
        });
    }

    private String calculatePeriodRangeTotalDays(String start, String end) {

        return String.valueOf(calculateDaysBetween(start, end) + 1);
    }

    // Calculate the days between start and end. Here the `start` and `end` are in DATE_SHORT_FORMAT.
    // "yyyy-mm-dd"
    private long calculateDaysBetween(String start, String end) {
        LocalDate dateBefore = LocalDate.parse(start);
        LocalDate dateAfter = LocalDate.parse(end);
        return ChronoUnit.DAYS.between(dateBefore, dateAfter);
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
        DatabaseReference flowLevelRef = mDatabase.child("period")
                .child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT)).child("flowLevel");
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
        DatabaseReference symptomsRef = mDatabase.child("period")
                .child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT)).child("symptoms");
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
        DatabaseReference moodRef = mDatabase.child("period")
                .child(convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT)).child("mood");
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

    private void setPeriodPredictionDate() {
        String selectedDateInStr = convertLocalDateToStringDate(selectedDate, DATE_SHORT_FORMAT);
        DatabaseReference periodRef = mDatabase.child("period");
        Query periodMonthQuery = periodRef.orderByChild("flowAndDate").endAt("1-" + selectedDateInStr).limitToLast(1);

        periodMonthQuery.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null && periodData.getHadFlow()) {
                        LocalDate date = LocalDate.parse(periodData.getStartDate());
                        // Add 28 days to her last period start day
                        long defaultRange = 28;
                        int times = (int) (calculateDaysBetween(periodData.getStartDate(), selectedDateInStr) / defaultRange + 1);
                        String predictedDate = convertLocalDateToStringDate(date.plusDays(defaultRange * times), DATE_LONG_FORMAT);
                        // If the predicted date is on the same year as selected date, only display MMMM dd.
                        if (Integer.parseInt(predictedDate.substring(predictedDate.length() - 4)) == selectedDate.getYear()) {
                            binding.periodPredictionDateTV.setText(predictedDate.substring(0, predictedDate.length() - 4));
                        } else {
                            binding.periodPredictionDateTV.setText(predictedDate);
                        }
                        return;
                    }
                }
                binding.periodPredictionDateTV.setText(R.string.no_record_string);
            }
        });
    }

    // Display the user selected date.
    private void setDateView() {
        binding.dateTV.setText(convertLocalDateToStringDate(selectedDate, DATE_FULL_FORMAT));
    }

    // Display the days of selected date's month on the calendar recycler view. Here the periodDatesInMonth
    // is empty array, its data has to be read from the database.
    private void setCalendarRecyclerView() {
        binding.monthYearBTN.setText(monthYearFromDate(selectedDate));
        daysOfMonth = generateDaysOfMonthArray(selectedDate);
        periodDatesInMonth = new ArrayList<>();

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

        // Set the Adapter for recyclerView, here the periodDatesInMonth is empty
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysOfMonth, this,
                selectedDate.getDayOfMonth(), selectedDateColor, selectedDateBackground, periodDatesInMonth, periodDatesColor, periodDatesBackground);
        binding.calendarRV.setAdapter(calendarAdapter);
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

    // Update user selected date. Convert the (year, month, day) to LocalDate format.
    private void setSelectedDate(int year, int month, int day) {
        selectedDate = selectedDate.plusDays(day - selectedDate.getDayOfMonth());
        selectedDate = selectedDate.plusMonths(month - (selectedDate.getMonthValue()));
        selectedDate = selectedDate.plusYears(year - selectedDate.getYear());
    }

    // Convert date short format to date long format
    private String dateShortToLongFormat(String date) {
        String[] dateSplit = date.split("-");
        String year = dateSplit[0];
        String month = monthValueToMonthName(Integer.parseInt(dateSplit[1]));
        String day = dateSplit[2];

        return month + " " + day + " " + year;
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

    // Convert milliseconds in UTC time to date in string
    private String convertUtcMillisecondsToDate(Long milliseconds, String dateFormat) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(milliseconds);
        SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(calendar.getTime());
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

    private String monthValueToMonthName(int month) {
        return new DateFormatSymbols().getMonths()[month - 1];
    }

    private int monthNameToMonthValue(String name) {
        return Month.valueOf(name.toUpperCase()).getValue();
    }

    private void moveRecord(DatabaseReference fromPath, final DatabaseReference toPath) {
    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            toPath.setValue(dataSnapshot.getValue()).addOnCompleteListener(task -> {
                if (task.isComplete()) {
                    Log.d(TAG, "Success!");
                    fromPath.removeValue();
                } else {
                    Log.d(TAG, "Copy failed!");
                }
            });
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d("TAG", databaseError.getMessage()); //Never ignore potential errors!
        }
    };
    fromPath.addListenerForSingleValueEvent(valueEventListener);
}
}
