package com.example.stayhealthy_android_app.Period;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
    private final static String DATE_LONG_FORMAT = "EEEE, MMMM d, yyyy";
    private final static String MONTH_YEAR_FORMAT = "MMMM yyyy";
    private TextView monthYearTV;
    private RecyclerView calendarRV;
    private LocalDate selectedDate;
    private TextView dateTV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period);

        // Initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

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

        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();
        setDateView();

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

    private void setMonthView() {
        monthYearTV.setText(monthYearFromDate(selectedDate));
        ArrayList<String> daysOfMonth = daysOfMonthArray(selectedDate);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRV.setLayoutManager(layoutManager);
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysOfMonth, this);
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
        setMonthView();
        setDateView();
    }

    // nextBTN onClickListener
    public void nextMonthAction(View view) {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
        setDateView();
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if (!dayText.equals("")) {
            selectedDate = selectedDate.plusDays(
                    Integer.parseInt(dayText) - selectedDate.getDayOfMonth());
            setDateView();
        }
    }
}
