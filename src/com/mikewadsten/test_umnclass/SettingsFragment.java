package com.mikewadsten.test_umnclass;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        loadPreferences();
    }

    private void loadPreferences() {
        addPreferencesFromResource(R.xml.preferences);
    }
}
