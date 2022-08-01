package com.example.stayhealthy_android_app.Diet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.example.stayhealthy_android_app.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class LunchActivity extends AppCompatActivity {
    private long protein;
    private long fat;
    private long carbs;
    private long netCal;
    private TextView proteinView;
    private TextView fatView;
    private TextView carbsView;
    private TextView netCalView;

    private DatabaseReference myDataBase;

    private ArrayList<FoodItem> foods = new ArrayList<>();
    private FoodAdapter foodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunch);
        myDataBase = FirebaseDatabase.getInstance().getReference("user").
                child("test@gmail_com").child("diets").child("20220731").child("lunch");

        initTextViews();
        loadValues();
    }

    private void createRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerView = findViewById(R.id.lunch_recycler_review);
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
    }

    @SuppressLint("SetTextI18n")
    private void loadValues() {
        myDataBase.get().addOnCompleteListener(task -> {
            HashMap tempMap = (HashMap) task.getResult().getValue();
            protein = (long) tempMap.get("protein");
            fat = (long) tempMap.get("fat");
            carbs = (long) tempMap.get("carbs");
            netCal = (long) tempMap.get("net");
            proteinView.setText("Protein: " + protein + " Cal");
            fatView.setText("Fat: " + fat + " Cal");
            carbsView.setText("Carbs: " + carbs + " Cal");
            netCalView.setText("Net Cal: " + netCal + " Cal");
            HashMap<String, HashMap<String, Long>> tempFoods = (HashMap) tempMap.get("foods");
            foods = new ArrayList<>();
            System.out.println("to process temp foods");
            for (String foodName : tempFoods.keySet()) {
                long foodProtein = tempFoods.get(foodName).get("protein");
                long foodFat = tempFoods.get(foodName).get("fat");
                long foodCarbs = tempFoods.get(foodName).get("carbs");
                foods.add(new FoodItem(foodName, "protein: " + foodProtein + " Cal; fat: " + foodFat + " Cal; carbs: " + foodCarbs + " Cal"));
            }
            createRecyclerView();
        });
    }

    private void initTextViews() {
        proteinView = findViewById(R.id.textView716);
        fatView = findViewById(R.id.textView710);
        carbsView = findViewById(R.id.textView717);
        netCalView = findViewById(R.id.textView77);
    }
}