package com.orelogo.relink;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by patso on 4/18/2016.
 */
public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    // strings for accessing preferences
    private String defaultTimeNumber = "defaultTimeNumber";
    private String defaultTimeScale = "defaultTimeScale";

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
        if (key.equals(defaultTimeNumber) || key.equals(defaultTimeScale)) {
            updateTimeSummaries();
        }
    }

    /**
     * Update the summaries and dialog title of the defaultTimeNumber and defaultTimeScale
     * preferences, based on the current SharedPreferences.
     */
    private void updateTimeSummaries() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        String number = preferences.getString(defaultTimeNumber, "4");
        String scale =
                MainActivity.getTimeScaleLong(preferences.getString(defaultTimeScale, "def"), true);

        String defaultReminderTime = "Reconnect every " + number + " " + scale;


        DialogPreference pref = (DialogPreference) findPreference(defaultTimeNumber);
        pref.setSummary(defaultReminderTime);
        pref.setDialogTitle("Reconnect every x " + scale);

        pref = (DialogPreference) findPreference(defaultTimeScale);
        pref.setSummary(defaultReminderTime);
        pref.setDialogTitle("Reconnect every " + number);

    }
}
