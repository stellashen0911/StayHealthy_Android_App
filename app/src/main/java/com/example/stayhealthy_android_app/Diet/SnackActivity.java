package com.example.stayhealthy_android_app.Diet;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.example.stayhealthy_android_app.R;

public class SnackActivity extends AppCompatActivity {
    private int protein;
    private int fat;
    private int carbs;
    private int netCal;
    private TextView proteinView;
    private TextView fatView;
    private TextView carbsView;
    private TextView netCalView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snack);
        loadValues();
        initTextViews();
        fillValues();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadValues();
        fillValues();
    }

    private void loadValues() {
        protein = 81;
        fat = 82;
        carbs = 83;
        netCal = 84;
    }

    private void initTextViews() {
        proteinView = findViewById(R.id.textView816);
        fatView = findViewById(R.id.textView810);
        carbsView = findViewById(R.id.textView817);
        netCalView = findViewById(R.id.textView87);
    }

    @SuppressLint("SetTextI18n")
    private void fillValues() {
        proteinView.setText("Protein: " + protein + " Cal");
        fatView.setText("Fat: " + fat + " Cal");
        carbsView.setText("Carbs: " + carbs + " Cal");
        netCalView.setText("Net Cal: " + netCal + " Cal");
    }
}