package com.orelogo.relink;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceFragment;


/**
 * Fragment for app settings.
 */
public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    // strings for accessing preferences
    static final String NOTIFY = "notify";
    static final String NOTIFY_INTERVAL = "notifyInterval";
    static final String DEFAULT_TIME_VALUE = "defaultTimeValue";
    static final String DEFAULT_TIME_SCALE = "defaultTimeScale";
    static final String DEFAULT_DELAY = "defaultDelay";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        // add listener for when shared preferences are changed
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // recreate alarm if notification settings change
        if (key.equals(NOTIFY) || key.equals(NOTIFY_INTERVAL)) {
            AlarmActivator.setAlarm(getActivity());
        }
    }

}
