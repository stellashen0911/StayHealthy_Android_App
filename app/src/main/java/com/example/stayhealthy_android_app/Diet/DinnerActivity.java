package com.example.stayhealthy_android_app.Diet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.example.stayhealthy_android_app.R;

import java.util.ArrayList;

public class DinnerActivity extends AppCompatActivity {
    private int protein;
    private int fat;
    private int carbs;
    private int netCal;
    private TextView proteinView;
    private TextView fatView;
    private TextView carbsView;
    private TextView netCalView;

    private ArrayList<FoodItem> foods = new ArrayList<>();
    private FoodAdapter foodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinner);
        loadValues();
        initTextViews();
        fillValues();

        createRecyclerView();
    }

    private void createRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerView = findViewById(R.id.dinner_recycler_review);
        recyclerView.setHasFixedSize(true);

        foodAdapter = new FoodAdapter(foods);
        FoodClickListener listener = position -> {
            foods.get(position).onLinkClicked(position);
            foodAdapter.notifyItemChanged(position);
        };

        foodAdapter.setFoodClickListener(listener);
        recyclerView.setAdapter(foodAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadValues();
        fillValues();
    }

    private void loadValues() {
        protein = 61;
        fat = 62;
        carbs = 63;
        netCal = 64;

        foods = new ArrayList<>();
        foods.add(new FoodItem("egg", "protein: 60 Cal; fat: 65 Cal; carbs: 60 Cal"));
        foods.add(new FoodItem("milk", "protein: 61 Cal; fat: 6 Cal; carbs: 61 Cal"));
        foods.add(new FoodItem("bread", "protein: 62 Cal; fat: 6 Cal; carbs: 66 Cal"));
    }

    private void initTextViews() {
        proteinView = findViewById(R.id.textView616);
        fatView = findViewById(R.id.textView610);
        carbsView = findViewById(R.id.textView617);
        netCalView = findViewById(R.id.textView67);
    }

    @SuppressLint("SetTextI18n")
    private void fillValues() {
        proteinView.setText("Protein: " + protein + " Cal");
        fatView.setText("Fat: " + fat + " Cal");
        carbsView.setText("Carbs: " + carbs + " Cal");
        netCalView.setText("Net Cal: " + netCal + " Cal");
    }
}