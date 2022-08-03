package com.example.stayhealthy_android_app;

import androidx.annotation.NonNull;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener;

public class AwardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView profile_nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_award);

        // Initialize and assign variable
        initWidgets();
        setBottomNavigationView();
        initProfileDrawer();
    }

    private void initProfileDrawer() {
        // Initialize profile drawer
        drawer = findViewById(R.id.drawer_layout);
        profile_nv = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        profile_nv.setNavigationItemSelectedListener(this);

        //set up the header button listeners
        View headerView = profile_nv.getHeaderView(0);
        Button LogOutBtn = (Button) headerView.findViewById(R.id.profile_logout_btn);
        Button ChangeAvartaButton = (Button) headerView.findViewById(R.id.update_profile_image_btn);

        LogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        ChangeAvartaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to do:
                System.out.println("clicked button");
                Intent i = new Intent(AwardActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });
    }
    private void initWidgets() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.award_icon);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setBottomNavigationView() {
        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.award_icon);

        // Perform item selected listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int selectedId = item.getItemId();
            boolean isItemSelected = false;
            if(selectedId == R.id.award_icon) {
                isItemSelected = true;
            } else if (selectedId == R.id.health_record_icon) {
                startActivity(new Intent(getApplicationContext(), HealthRecordActivity.class));
                overridePendingTransition(0,0);
                isItemSelected = true;
            } else if (selectedId == R.id.journey_icon) {
                startActivity(new Intent(getApplicationContext(), JourneyActivity.class));
                overridePendingTransition(0, 0);
                isItemSelected = true;
            }

            return isItemSelected;
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = null;
        switch(item.getItemId()) {
            case R.id.nav_settings:
                drawer.closeDrawers();
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
            case R.id.nav_health_records:
                drawer.closeDrawers();
                startActivity(new Intent(getApplicationContext(), HealthRecordActivity.class));
                break;
            case R.id.nav_award:
                drawer.closeDrawers();
                startActivity(new Intent(getApplicationContext(), AwardActivity.class));
                break;
            case R.id.nav_journey:
                drawer.closeDrawers();
                startActivity(new Intent(getApplicationContext(), JourneyActivity.class));
                break;
        }

//        if (fragment != null) {
//            // Insert the fragment by replacing any existing fragment
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            fragmentTransaction.replace(R.id.fragment_container, fragment);
//            fragmentManager.popBackStack();
//            fragmentTransaction.commit();
//            // Highlight the selected item has been done by NavigationView
//            item.setChecked(true);
//            // Set action bar title
//            setTitle(item.getTitle());
//            // Close the navigation drawer
//            drawer.closeDrawer(GravityCompat.START);
//        }
        System.out.println("here 6");
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}