package com.orelogo.relink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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

        EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
        connectIntervalField.setText(Double.toString(connectInterval));

        loadSpinner(timeScale);
    }

    /**
     * Load spinner with selection based on timeScale.
     */
    private void loadSpinner(String timeScale) {
        timeScaleSpinner = (Spinner) findViewById(R.id.time_scale_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.time_scale_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeScaleSpinner.setAdapter(adapter);

        // set spinner selection
        timeScaleSpinner.setSelection(Convert.getSpinnerSelection(timeScale));
    }

    /**
     * Load text of delay button based on preferences.
     */
    private void loadDelayButton() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String delayTimeScale = Convert.getTimeScaleLong(
                preferences.getString(SettingsFragment.DEFAULT_DELAY, Convert.WEEKS_CHAR), false);
        String text = getResources().getString(R.string.delay_1) + " " + delayTimeScale;

        TextView button = (TextView) findViewById(R.id.delay_button);
        button.setText(text);
    }

    public void connected(View view) {
        if (isInputValid()) {

            // get connect time interval from user input
            EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
            Double connectInterval = Double.parseDouble(
                    connectIntervalField.getText().toString());

            // get time in milliseconds based on user selected time scale spinner
            long timeMs; // time in a day, week, month, or year (in ms)
            String timeScale; // time scale to be used with interval value
            String spinnerValue = timeScaleSpinner.getSelectedItem().toString();

            switch (spinnerValue) {
                case Convert.DAYS_PLURAL:
                    timeMs = Convert.DAY_MS;
                    timeScale = Convert.DAYS_CHAR;
                    break;
                case Convert.WEEKS_PLURAL:
                    timeMs = Convert.WEEK_MS;
                    timeScale = Convert.WEEKS_CHAR;
                    break;
                case Convert.MONTHS_PLURAL:
                    timeMs = Convert.MONTH_MS;
                    timeScale = Convert.MONTHS_CHAR;
                    break;
                case Convert.YEARS_PLURAL:
                    timeMs = Convert.YEAR_MS;
                    timeScale = Convert.YEARS_CHAR;
                    break;
                default: // an error occurred
                    timeMs = 0;
                    timeScale = "x";
                    break;
            }

            // calculate time to next connect
            long currentTime =  System.currentTimeMillis();
            long intervalTime = (long) Math.floor(connectInterval * timeMs);
            long nextConnect = currentTime + intervalTime;// time for next connect (in milliseconds)

            // add name and next connect time to database
            db.open();
            db.updateRow(rowId, name, currentTime, nextConnect, connectInterval, timeScale);
            db.close();
            finish(); // finish activity
        }
    }

    /**
     * Determines if user input is valid.
     *
     * @return true if user supplied valid input, otherwise, false
     */
    private boolean isInputValid() {

        // field to display error message
        TextView errorField = (TextView) findViewById(R.id.error);
        String errorText = ""; // error message

        // check if user supplied a valid number, 0 considered valid
        EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
        try {
            Double connectInterval = Double.parseDouble(connectIntervalField.getText().toString());
            if (connectInterval < 0) {
                errorText += getResources().getString(R.string.valid_number);
            }
        }
        catch (NumberFormatException e) { // occurs if no value is entered
            errorText += getResources().getString(R.string.valid_number);
        }

        if (errorText.length() == 0) {
            return true;
        }
        else {
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
                SettingsFragment.DEFAULT_DELAY, Convert.WEEKS_CHAR);

        delayedNextConnect += Convert.getMillisec(defaultDelay);

        // add name and next connect time to database
        db.open();
        db.updateRow(rowId, name, lastConnect, delayedNextConnect, connectInterval, timeScale);
        db.close();
        finish(); // finish activity


    }

}
