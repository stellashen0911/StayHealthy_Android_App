package com.example.stayhealthy_android_app.Diet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.example.stayhealthy_android_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class BreakfastActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_breakfast);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        myDataBase = FirebaseDatabase.getInstance().getReference("user").
                child("test@gmail_com").child("diets").child("20220731").child("breakfast");
        initTextViews();
        loadValues();
    }

    private void createRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerView = findViewById(R.id.breakfast_recycler_review);
        recyclerView.setHasFixedSize(true);

        foodAdapter = new FoodAdapter(foods);
        FoodClickListener listener = position -> {
            foods.get(position).onFoodClicked(position);
            foodAdapter.notifyItemChanged(position);
        };

        FoodCheckedListener foodCheckedListener = (position, isChecked) -> {
            foods.get(position).setFoodChecked(isChecked);

            protein = 0;
            fat = 0;
            carbs = 0;
            netCal = 0;

            for (FoodItem food :foods) {
                long foodProtein = food.getFoodChecked() ? food.getProtein() : 0;
                long foodFat = food.getFoodChecked() ? food.getFat() : 0;
                long foodCarbs = food.getFoodChecked() ? food.getCarbs() : 0;
                protein += foodProtein;
                fat += foodFat;
                carbs += foodCarbs;
                netCal += (foodProtein + foodFat + foodCarbs);
            }

            proteinView.setText("Protein: " + protein + " Cal");
            fatView.setText("Fat: " + fat + " Cal");
            carbsView.setText("Carbs: " + carbs + " Cal");
            netCalView.setText("Net Cal: " + netCal + " Cal");

            myDataBase.child("protein").setValue(protein);
            myDataBase.child("fat").setValue(fat);
            myDataBase.child("carbs").setValue(carbs);
            myDataBase.child("net").setValue(netCal);
            myDataBase.child("foods").child(foods.get(position).getFoodName()).child("checked").setValue(isChecked ? 1 : 0);
        };

        foodAdapter.setFoodClickListener(listener, foodCheckedListener);
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
                long foodChecked = tempFoods.get(foodName).get("checked");
                boolean checked = foodChecked == 1 ? true : false;
                foods.add(new FoodItem(foodName, foodProtein, foodFat, foodCarbs, checked));
            }
            createRecyclerView();
        });
    }

    private void initTextViews() {
        proteinView = findViewById(R.id.textView16);
        fatView = findViewById(R.id.textView10);
        carbsView = findViewById(R.id.textView17);
        netCalView = findViewById(R.id.textView7);
    }

}