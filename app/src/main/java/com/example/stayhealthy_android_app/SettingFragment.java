package com.example.stayhealthy_android_app;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private DatabaseReference mDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // below line is used to add preference
        // fragment from our xml folder.
        addPreferencesFromResource(R.xml.setting_fragment);

        // Get the current user from firebase authentication.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        bindPreferenceSummaryToValue(findPreference("switch_preference_workout"));
        bindPreferenceSummaryToValue(findPreference("switch_preference_water"));
        bindPreferenceSummaryToValue(findPreference("switch_preference_period"));
        bindPreferenceSummaryToValue(findPreference("switch_preference_diet"));

    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), true));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String current_preference = preference.getTitle().toString();
        boolean state = Boolean.valueOf(newValue.toString());
        if (state) {
            mDatabase.child("notification_settings").child(current_preference).setValue(true);
        } else {
            mDatabase.child("notification_settings").child(current_preference).setValue(false);
        }
        return true;
    }
}
