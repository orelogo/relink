package com.orelogo.relink;

import android.content.Intent;
import android.os.Bundle;
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
     * Load spinner with selection based on timeScale char.
     */
    private void loadSpinner(String timeScale) {
        timeScaleSpinner = (Spinner) findViewById(R.id.time_scale_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.time_scale_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeScaleSpinner.setAdapter(adapter);

        // choose spinner selection based on timeScale value
        int spinnerSelection;
        switch (timeScale) {
            case (MainActivity.DAYS):
                spinnerSelection = 0;
                break;
            case (MainActivity.WEEKS):
                spinnerSelection = 1;
                break;
            case (MainActivity.MONTHS):
                spinnerSelection = 2;
                break;
            case (MainActivity.YEARS):
                spinnerSelection = 3;
                break;
            default: // error occured
                spinnerSelection = -1;
                break;
        }
        timeScaleSpinner.setSelection(spinnerSelection); // set selection to "Months"
    }

    public void connected(View view) {
        if (isInputValid()) {

            // get connect time interval from user input
            EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
            Double connectInterval = Double.parseDouble(
                    connectIntervalField.getText().toString());

            // get time in milliseconds based on user selected time scale spinner
            long time_ms; // time in a day, week, month, or year (in ms)
            String timeScale; // time scale to be used with interval value
            String spinnerValue = timeScaleSpinner.getSelectedItem().toString();

            switch (spinnerValue) {
                case "Days":
                    time_ms = MainActivity.DAY_MS;
                    timeScale = MainActivity.DAYS;
                    break;
                case "Weeks":
                    time_ms = MainActivity.WEEK_MS;
                    timeScale = MainActivity.WEEKS;
                    break;
                case "Months":
                    time_ms = MainActivity.MONTH_MS;
                    timeScale = MainActivity.MONTHS;
                    break;
                case "Years":
                    time_ms = MainActivity.YEAR_MS;
                    timeScale = MainActivity.YEARS;
                    break;
                default: // an error occurred
                    time_ms = 0;
                    timeScale = "x";
                    break;
            }

            // calculate time to next connect
            long currentTime =  System.currentTimeMillis();
            long intervalTime = (long) Math.floor(connectInterval * time_ms);
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
                errorText += "Please enter a valid number.";
            }
        }
        catch (NumberFormatException e) { // occurs if no value is entered
            errorText += "Please enter a valid number.";
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
     * Delay reminder by 1 week.
     */
    public void delayReminder(View view) {

        long delayedNextConnect; // unix time for delayed next connect time

        long currentTime =  System.currentTimeMillis();
        if (nextConnect > currentTime) { // if reinder is now due yet
            delayedNextConnect = nextConnect;
        }
        else {                           // reminder is already due
            delayedNextConnect = currentTime;
        }

        delayedNextConnect += MainActivity.WEEK_MS;

        // add name and next connect time to database
        db.open();
        db.updateRow(rowId, name, lastConnect, delayedNextConnect, connectInterval, timeScale);
        db.close();
        finish(); // finish activity


    }

}
