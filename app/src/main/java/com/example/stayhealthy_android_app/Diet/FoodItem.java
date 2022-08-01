package com.example.stayhealthy_android_app.Diet;

public class FoodItem implements FoodClickListener {
    final private String foodName;
    final private String foodInfo;

    public FoodItem(String foodName, String foodInfo) {
        this.foodName = foodName;
        this.foodInfo = foodInfo;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getFoodInfo() {
        return foodInfo;
    }

    @Override
    public void onLinkClicked(int position) {

    }
}
