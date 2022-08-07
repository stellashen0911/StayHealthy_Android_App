package com.example.stayhealthy_android_app.Award.Model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class AwardDisplay {
    private final String name;
    // For daily award, the detail is "Today". For long term award, the detail is in `num` Days format.
    private final String details;

    public AwardDisplay(String name, String details) {
        this.name = name;
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public String getDetails() {
        return details;
    }

    @NonNull
    @Override
    public String toString() {
        return  "Awards name: " + getName() + " Details: " + getDetails();
    }
}
