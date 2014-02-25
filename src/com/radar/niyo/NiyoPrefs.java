package com.radar.niyo;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class NiyoPrefs extends PreferenceFragment {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.niyo_prefs);
        
    }
}
