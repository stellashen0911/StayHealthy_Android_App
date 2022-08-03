package com.example.stayhealthy_android_app;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import androidx.annotation.Nullable;

public class SettingFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // below line is used to add preference
        // fragment from our xml folder.
        System.out.println("here settings fragment start");
        addPreferencesFromResource(R.xml.setting_fragment);
        System.out.println("here settings fragment end");
    }
}
