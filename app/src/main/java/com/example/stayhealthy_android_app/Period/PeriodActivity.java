package com.example.stayhealthy_android_app.Period;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
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
import com.example.stayhealthy_android_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class PeriodActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{
    private final static String TAG = "PeriodActivity";
    private final static String DATE_LONG_FORMAT = "EEEE, MMMM d, yyyy";
    private final static String MONTH_YEAR_FORMAT = "MMMM yyyy";
    private final static int DAY_TO_MILLISECONDS = 86400000; // that is: 24 * 60 * 60 * 1000
    private BottomNavigationView bottomNavigationView;
    private Button monthYearBTN;
    private RecyclerView calendarRV;
    private LocalDate selectedDate;
    private TextView dateTV;
    private TextView periodDateDetailsTV;
    private TextView periodFlowDetailsTV;
    private TextView symptomsDetailsTV;
    private EmojiTextView moodEmojiTV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period);

        // Initialize the selected date as today
        selectedDate = LocalDate.now();

        initWidgets();
        setBottomNavigationView();

        // Update UI with the current selected Date. The information displayed on the screen depends
        // on the selected date.
        updateUI();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.health_record_icon);
    }

    // addPeriodDateBTN onClickListener. A datePicker shown when clicked. User can choose a date or
    // a range of dates through this picker.
    public void addPeriodDate(View view) {
        // Set default selection range as 2 days ago to today. StartDate is 4 days ago before the
        // end date. TODO: This number of days ago can be set as the specific user period range. Typical period lasts for 5 days
        int daysAgo = 4;
        Long endDateInMilliseconds = selectedDateInMilliseconds();
        Long startDateInMilliseconds = daysAgoInMilliseconds(endDateInMilliseconds, daysAgo);
        final MaterialDatePicker<Pair<Long, Long>> materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Period Range")
                .setSelection(new Pair<>(startDateInMilliseconds, endDateInMilliseconds))
                .build();
        materialDatePicker.show(getSupportFragmentManager(), "PeriodDateRangePicker");

        materialDatePicker.addOnPositiveButtonClickListener(
                (MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>) selection
                        -> periodDateDetailsTV.setText(materialDatePicker.getHeaderText()));
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
                        -> periodFlowDetailsTV.setText(flowLevelOptions[checkedFlowLevel[0]]))
                .setNegativeButton(R.string.cancel_string, null)
                .create()
                .show();
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
                    symptomsDetailsTV.setText(stringBuilder);})
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
                        moodEmojiTV.setTextColor(ContextCompat.getColor(this, R.color.emoji_color));})
                .setNegativeButton(R.string.cancel_string, null)
                .setView(gridView)
                .create()
                .show();
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

    // interface in CalendarAdapter.OnItemListener. Used to update date by selected day on the
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
        monthYearBTN = findViewById(R.id.monthYearBTN);
        calendarRV = findViewById(R.id.calendarRV);
        dateTV = findViewById(R.id.dateTV);
        periodDateDetailsTV = findViewById(R.id.periodDateDetailsTV);
        periodFlowDetailsTV = findViewById(R.id.periodFlowDetailsTV);
        symptomsDetailsTV = findViewById((R.id.symptomsDetailsTV));
        moodEmojiTV = findViewById(R.id.moodEmojiTV);
    }

    private void updateUI() {
        // Set the days of month on the calendar recycler view
        setDaysOfMonthRecyclerView();
        // Set the selected date view
        setDateView();
    }

    // Display the user selected date.
    private void setDateView() {
        dateTV.setText(convertLocalDateToLongFormatStringDate(selectedDate));
    }

    // Display the days of month array on the calendar recycler view.
    private void setDaysOfMonthRecyclerView() {
        monthYearBTN.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysOfMonth = daysOfMonthArray(selectedDate);

        // Set the LayoutManager for recyclerView
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRV.setLayoutManager(layoutManager);

        // Set the Adapter for recyclerView
        int selectedDayColor = getColor(R.color.black);
        Drawable selectedDayBackground = ResourcesCompat.getDrawable(getResources(),
                R.drawable.m_customer_circle_gray_drawable, null);
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysOfMonth, this,
                selectedDate.getDayOfMonth(), selectedDayColor, selectedDayBackground);
        calendarRV.setAdapter(calendarAdapter);
    }

    // Create the days of month array according to the user selected month. Used as the item list in
    // calendar recycler view.
    private ArrayList<String> daysOfMonthArray(LocalDate date) {
        ArrayList<String> daysOfMonthArray = new ArrayList<>();
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

    // Convert LocalDate to date in long format string.
    private String convertLocalDateToLongFormatStringDate(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_LONG_FORMAT);
        return date.format(dateTimeFormatter);
    }

    // Get month year string from LocalDate
    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT);
        return date.format(dateTimeFormatter);
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

    // Get 2 days ago in milliseconds. `today` is in milliseconds format.
    private Long daysAgoInMilliseconds(Long today, int daysAgo) {
        return today - (long) daysAgo * DAY_TO_MILLISECONDS;
    }
}
