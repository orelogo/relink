package com.orelogo.relink;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    // strings for accessing preferences
    static final String DEFAULT_TIME_NUMBER = "defaultTimeNumber";
    static final String DEFAULT_TIME_SCALE = "defaultTimeScale";
    static final String DEFAULT_DELAY = "defaultDelay";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        // add listener for when shared preferences are changed
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        updateTimeSummaries();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(DEFAULT_TIME_NUMBER) || key.equals(DEFAULT_TIME_SCALE)) {
            updateTimeSummaries();
        }
    }

    /**
     * Update the summaries and dialog title of the defaultTimeNumber and defaultTimeScale
     * preferences, based on the current SharedPreferences.
     */
    private void updateTimeSummaries() {
        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();

        String number = preferences.getString(DEFAULT_TIME_NUMBER,
                getResources().getString(R.string.time_number_default));
        String scale =
                Convert.getTimeScaleLong(preferences.getString(DEFAULT_TIME_SCALE, "def"), true);

        String defaultReminderTime = getResources().getString(R.string.reconnect_every) + " " +
                number + " " + scale;


        DialogPreference pref = (DialogPreference) findPreference(DEFAULT_TIME_NUMBER);
        pref.setSummary(defaultReminderTime);
        pref.setDialogTitle(getResources().getString(R.string.reconnect_every) + " x " + scale);

        pref = (DialogPreference) findPreference(DEFAULT_TIME_SCALE);
        pref.setSummary(defaultReminderTime);
        pref.setDialogTitle(getResources().getString(R.string.reconnect_every) + " " + number);

    }
}
