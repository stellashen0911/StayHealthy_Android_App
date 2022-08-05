package com.example.stayhealthy_android_app.Period;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.stayhealthy_android_app.R;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Objects;

// The MonthYearPicker class represents the month year picker. The class creates a dialog where
// user can select month and year.
public class MonthYearPickerDialog extends DialogFragment {
    private static final int MIN_YEAR = 1901, MAX_MONTH = 12, MIN_MONTH = 1;
    public static final String MONTH_KEY = "monthValue";
    public static final String DAY_KEY = "dayValue";
    public static final String YEAR_KEY = "yearValue";
    private DatePickerDialog.OnDateSetListener listener;
    private NumberPicker monthPicker, yearPicker;
    private int currMonth, currDay, currYear;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getArguments();
        if(extras != null){
            currMonth = extras.getInt(MONTH_KEY , -1);
            currDay = extras.getInt(DAY_KEY , -1);
            currYear = extras.getInt(YEAR_KEY , -1);
        }
    }

    public static MonthYearPickerDialog newInstance(int monthIndex , int daysIndex , int yearIndex) {
        MonthYearPickerDialog monthYearPickerDialog = new MonthYearPickerDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt(MONTH_KEY, monthIndex);
        args.putInt(DAY_KEY, daysIndex);
        args.putInt(YEAR_KEY, yearIndex);
        monthYearPickerDialog.setArguments(args);

        return monthYearPickerDialog;
    }

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialog = inflater.inflate(R.layout.month_year_picker, null);
        monthPicker = dialog.findViewById(R.id.picker_month);
        yearPicker = dialog.findViewById(R.id.picker_year);

        monthPicker.setMinValue(MIN_MONTH);
        monthPicker.setMaxValue(MAX_MONTH);
        monthPicker.setValue(currMonth);
        String[] month = new String[MAX_MONTH - MIN_MONTH + 1];
        for(int i = 0; i < 12; i++) {
            month[i] = getMonth(i);
        }
        monthPicker.setDisplayedValues(month);

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        yearPicker.setMinValue(MIN_YEAR);
        yearPicker.setMaxValue(year);
        yearPicker.setValue(currYear);

        builder.setView(dialog)
                // Add action buttons
                .setPositiveButton(R.string.ok_string, (dialog1, id) -> listener.onDateSet(null, yearPicker.getValue(), monthPicker.getValue(), currDay))
                .setNegativeButton(R.string.cancel_string, (dialog12, id) -> Objects.requireNonNull(MonthYearPickerDialog.this.getDialog()).cancel())
                .setTitle(R.string.select_month_year_string);
        return builder.create();
    }

    // Convert month value [0, 11] to month label
    private String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month];
    }
}
