package com.example.stayhealthy_android_app.Period;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stayhealthy_android_app.AwardActivity;
import com.example.stayhealthy_android_app.JourneyActivity;
import com.example.stayhealthy_android_app.Period.Calendar.CalendarAdapter;
import com.example.stayhealthy_android_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class PeriodActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{
    private final static String TAG = "PeriodActivity";
    private final static String DATE_LONG_FORMAT = "EEEE, MMMM d, yyyy";
    private final static String MONTH_YEAR_FORMAT = "MMMM yyyy";
    private BottomNavigationView bottomNavigationView;
    private TextView monthYearTV;
    private RecyclerView calendarRV;
    private LocalDate selectedDate;
    private TextView dateTV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period);

        // Initialize the selected date as today
        selectedDate = LocalDate.now();

        initWidgets();
        setBottomNavigationView();
        // Set the days of month on the calendar recycler view
        setDaysOfMonthView();
        // Set the selected date view
        setDateView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.health_record_icon);
    }

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

    private void initWidgets() {
        monthYearTV = findViewById(R.id.monthYearTV);
        calendarRV = findViewById(R.id.calendarRV);
        dateTV = findViewById(R.id.dateTV);

    }

    private void setDateView() {
        dateTV.setText(dateFromDate(selectedDate));
    }

    private String dateFromDate(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_LONG_FORMAT);
        return date.format(dateTimeFormatter);
    }

    private void setDaysOfMonthView() {
        monthYearTV.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysOfMonth = daysOfMonthArray(selectedDate);

        // Set the LayoutManager for recyclerView
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRV.setLayoutManager(layoutManager);

        // Set the Adapter for recyclerView
        int selectedDayColor = getColor(R.color.white);
        Drawable selectedDayBackground = ResourcesCompat.getDrawable(getResources(),
                R.drawable.m_customer_circle_red_drawable, null);
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysOfMonth, this,
                selectedDate.getDayOfMonth(), selectedDayColor, selectedDayBackground);
        calendarRV.setAdapter(calendarAdapter);
    }

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

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT);
        return date.format(dateTimeFormatter);
    }

    // previousBTN onClickListener
    public void previousMonthAction(View view) {
        selectedDate = selectedDate.minusMonths(1);
        setDaysOfMonthView();
        setDateView();
    }

    // nextBTN onClickListener
    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1);
        setDaysOfMonthView();
        setDateView();
    }

    @Override
    public void onItemClick(int position, TextView dateTV) {
        if (!dateTV.getText().equals("")) {
            selectedDate = selectedDate.plusDays(
                    Integer.parseInt((String)dateTV.getText()) - selectedDate.getDayOfMonth());
            setDaysOfMonthView();
            setDateView();
        }
    }
}
