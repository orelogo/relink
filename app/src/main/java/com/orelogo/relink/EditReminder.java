package com.orelogo.relink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Activity for editing a reminder.
 */
public class EditReminder extends AppCompatActivity {

    private DBAdapter db = new DBAdapter(this); // database adapter for interacting with database
    private Spinner timeScaleSpinner; // spinner for selecting time scale

    // reminder information
    private int rowId;
    private String name;
    private long lastConnect;
    private long nextConnect;
    private double connectInterval;
    private String timeScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_reminder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // enable up button in app bar
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        displayReminderData();
        loadDelayButton();
    }

    private void displayReminderData() {
        Intent intent = getIntent();

        rowId = intent.getIntExtra(MainActivity.ROW_ID, -1);
        name = intent.getStringExtra(MainActivity.NAME);
        lastConnect = intent.getLongExtra(MainActivity.LAST_CONNECT, -1);
        nextConnect = intent.getLongExtra(MainActivity.NEXT_CONNECT, -1);
        connectInterval = intent.getDoubleExtra(MainActivity.CONNECT_INTERVAL, -1);
        timeScale = intent.getStringExtra(MainActivity.TIME_SCALE);

        TextView nameField = (TextView) findViewById(R.id.name);
        nameField.setText(name);

        TextView nextConnectField = (TextView) findViewById(R.id.next_connect);
        nextConnectField.setText(MainActivity.getTimeRemaining(nextConnect));

        EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
        connectIntervalField.setText(Double.toString(connectInterval));

        loadSpinner();
    }

    /**
     * Load spinner with selection based on time scale stored in the reminder.
     */
    private void loadSpinner() {
        timeScaleSpinner = (Spinner) findViewById(R.id.time_scale_spinner);
        AddReminder.loadTimeScaleSpinner(this, timeScaleSpinner, timeScale);
    }

    /**
     * Load text of delay button based on preferences.
     */
    private void loadDelayButton() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String delayTimeChar = preferences.getString(SettingsFragment.DEFAULT_DELAY,
                getResources().getString(R.string.week_default));
        String delayTimeLong = Convert.getTimeScaleLong(delayTimeChar, false);
        String text = getResources().getString(R.string.delay_1) + " " + delayTimeLong;

        TextView button = (TextView) findViewById(R.id.delay_button);
        button.setText(text);
    }

    /**
     * Update database entry with new connect time.
     * @param view
     */
    public void connected(View view) {

        if (isInputValid()) {

            // get connect time interval from user input
            EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
            Double connectInterval = Double.parseDouble(
                    connectIntervalField.getText().toString());

            // get time in milliseconds based on user selected time scale spinner
            String spinnerValue = timeScaleSpinner.getSelectedItem().toString();
            // time scale to be used with interval value
            String timeScale = Convert.getTimeScaleChar(spinnerValue);

            // calculate time to next connect
            long currentTime =  System.currentTimeMillis();
            long nextConnect = Convert.getNextConnect(connectInterval, timeScale);

            // add name and next connect time to database
            db.open();
            db.updateRow(rowId, name, currentTime, nextConnect, connectInterval, timeScale);
            db.close();
            finish(); // finish activity
        }
    }

    /**
     * Determines if user input is valid. If input is invalid, an error message is displayed.
     *
     * @return true if user supplied valid input, otherwise false
     */
    private boolean isInputValid() {

        // check if user supplied a valid number, 0 considered valid
        EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);

        // error message, may return an empty string
        String errorText = AddReminder.errorTimeValue(this, connectIntervalField);

        if (errorText.length() == 0) {
            return true;
        }
        else {
            // field to display error message
            TextView errorField = (TextView) findViewById(R.id.error);
            errorField.setText(errorText); // display error message
            return false;
        }
    }

    /**
     * Delete reminder.
     *
     * @param view
     */
    public void deleteReminder(View view) {
        db.open();
        db.deleteRow(rowId);
        db.close();
        finish();
    }

    /**
     * Delay reminder by 1 day/week/month/year, depending on preferences.
     */
    public void delayReminder(View view) {

        long delayedNextConnect; // unix time for delayed next connect time

        long currentTime =  System.currentTimeMillis();
        if (nextConnect > currentTime) { // if reminder is not due yet
            delayedNextConnect = nextConnect;
        }
        else {                           // reminder is already due
            delayedNextConnect = currentTime;
        }

        // get delay button time scale from preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultDelay = preferences.getString(
                SettingsFragment.DEFAULT_DELAY, getResources().getString(R.string.week_default));

        delayedNextConnect += Convert.getMillisec(defaultDelay);

        // add name and next connect time to database
        db.open();
        db.updateRow(rowId, name, lastConnect, delayedNextConnect, connectInterval, timeScale);
        db.close();
        finish(); // finish activity
    }

}
