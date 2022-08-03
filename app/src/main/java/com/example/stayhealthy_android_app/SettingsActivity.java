package com.example.stayhealthy_android_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");

        // below line is used to check if
        // frame layout is empty or not.
        System.out.println("here settings activity start");
        if (findViewById(R.id.idFrameLayoutSettings) != null) {
            if (savedInstanceState != null) {
                return;
            }
            System.out.println("here settings activity fragment start");
            // below line is to inflate our fragment.
            getFragmentManager().beginTransaction().add(R.id.idFrameLayoutSettings, new SettingFragment()).commit();
            System.out.println("here settings activity end");
        }
    }
}