package com.example.stayhealthy_android_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class AwardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView nav_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_award);

        // Initialize and assign variable
        initWidgets();
        setBottomNavigationView();

        // Initialize toolbar
        initProfile();
        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);
    }

    private void initProfile() {
        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
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

//    private void setupDrawerContent(NavigationView navigationView) {
//        System.out.println("here start setup");
//        navigationView.setNavigationItemSelectedListener(
//                new NavigationView.OnNavigationItemSelectedListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(MenuItem menuItem) {
//                        System.out.println("here 1");
//                        selectDrawerItem(menuItem);
//                        return true;
//                    }
//                });
//    }

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = null;
        System.out.println("here 2");
        switch(item.getItemId()) {
            case R.id.nav_settings:
                System.out.println("here 4");
                fragmentClass = SettingFragment.class;
            case R.id.nav_health_records:
                System.out.println("here 3");
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
            default:
                fragmentClass = null;
                drawer.closeDrawers();
        }

        if (fragment != null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("here 5");
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();

            // Highlight the selected item has been done by NavigationView
            item.setChecked(true);
            // Set action bar title
            setTitle(item.getTitle());
            // Close the navigation drawer
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }
}