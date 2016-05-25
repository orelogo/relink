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
        updateTimeSummaries();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // update summaries of time value and time scale
        if (key.equals(DEFAULT_TIME_VALUE) || key.equals(DEFAULT_TIME_SCALE)) {
            updateTimeSummaries();
        }
        // recreate alarm if notification settings change
        if (key.equals(NOTIFY) || key.equals(NOTIFY_INTERVAL)) {
            AlarmActivator.createAlarm(getActivity());
        }
    }

    /**
     * Update the summaries and dialog title of the defaultTimeValue and defaultTimeScale
     * preferences, based on the current SharedPreferences.
     */
    private void updateTimeSummaries() {
        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();

        String number = preferences.getString(DEFAULT_TIME_VALUE,
                getResources().getString(R.string.time_value_default));
        String scale = Convert.getTimeScaleLong(preferences.getString(DEFAULT_TIME_SCALE,
                getResources().getString(R.string.week_default)), true);

        String defaultReminderTime = getResources().getString(R.string.reconnect_every) + " " +
                number + " " + scale;


        DialogPreference pref = (DialogPreference) findPreference(DEFAULT_TIME_VALUE);
        pref.setSummary(defaultReminderTime);
        pref.setDialogTitle(getResources().getString(R.string.reconnect_every) + " x " + scale);

        pref = (DialogPreference) findPreference(DEFAULT_TIME_SCALE);
        pref.setSummary(defaultReminderTime);
        pref.setDialogTitle(getResources().getString(R.string.reconnect_every) + " " + number);

    }
}
