package com.example.stayhealthy_android_app.Diet;

public class FoodItem implements FoodClickListener {
    final private String foodName;
    final private long protein;
    final private long fat;
    final private long carbs;
    final private String foodInfo;
    private boolean checked;


    public FoodItem(String foodName, long protein, long fat, long carbs, boolean checked) {
        this.foodName = foodName;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.foodInfo = "protein: " + protein + " Cal; fat: " + fat + " Cal; carbs: " + carbs + " Cal";
        this.checked = checked;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getFoodInfo() {
        return foodInfo;
    }

    public void setFoodChecked(boolean isChecked) {
        this.checked = isChecked;
    }

    public long getProtein() {
        return protein;
    }

    public long getFat() {
        return fat;
    }

    public long getCarbs() {
        return carbs;
    }

    public boolean getFoodChecked() {return checked;}

    @Override
    public void onFoodClicked(int position) {

    }
}
