package com.orelogo.relink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Activity for adding a new reminder to the app.
 */
public class AddReminder extends AppCompatActivity {

    private Spinner timeScaleSpinner; // spinner for selecting time scale
    private final int PICK_CONTACT_REQUEST = 1;  // request code for picking a contact via Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadConnectInterval();
        loadSpinner();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Load spinner for selecting time scale for input (days, weeks, moths, years).
     */
    private void loadSpinner() {
        timeScaleSpinner = (Spinner) findViewById(R.id.time_scale_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.time_scale_plural, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeScaleSpinner.setAdapter(adapter);

        // set spinner selection based on preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String timeScale = preferences.getString(
                SettingsFragment.DEFAULT_TIME_SCALE, Convert.MONTHS_CHAR);
        int spinnerSelection = Convert.getSpinnerSelection(timeScale);

        timeScaleSpinner.setSelection(spinnerSelection);
    }

    /**
     * Load connect interval field based on shared preferences.
     */
    private void loadConnectInterval() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String timeValue = preferences.getString(SettingsFragment.DEFAULT_TIME_VALUE,
                getResources().getString(R.string.time_value_default));

        EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
        connectIntervalField.setText(timeValue);
    }

    /**
     * Pick a contact from your contacts via an intent.
     *
     * @param view view that was clicked
     */
    public void pickContact(View view) {
        Intent pickContactIntent = new Intent(
                Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    /**
     * When the intent returns from the contacts app, display the contact's name in the name field.
     *
     * @param requestCode request code sent with intent
     * @param resultCode result code received from contacts app
     * @param data intent received from contacts app with contact uri
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // ensure the request was successful
            if (resultCode == RESULT_OK) {
                Uri contactUri = data.getData(); // get uri of selected contact
                // get only data from display name column
                String[] projection = {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};

                // get cursor with data from uri
                Cursor cursor = getContentResolver().query(
                        contactUri, projection, null, null, null);
                cursor.moveToFirst();

                // get name from cursor
                int column = cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
                String name = cursor.getString(column);
                cursor.close();

                // set name field to contact name
                EditText nameField = (EditText) findViewById(R.id.name);
                nameField.setText(name);

            }
        }
    }

    /**
     * Add reminder information to database.
     *
     * @param view view that was clicked
     */
    public void addReminder(View view) {

        if (isInputValid()) {
            // get name from user
            EditText nameField = (EditText) findViewById(R.id.name);
            String name = nameField.getText().toString();

            // get connect time interval from user
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
            DBAdapter db = new DBAdapter(this); // database adapter for interacting with database
            db.open();
            db.insertRow(name, currentTime, nextConnect, connectInterval, timeScale);
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

        // check if user supplied a name
        EditText nameField = (EditText) findViewById(R.id.name);
        String name = nameField.getText().toString();
        if (name.length() == 0 ) {
            errorText += getResources().getString(R.string.valid_name);
        }

        // check if user supplied a valid number, 0 considered valid
        EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
        try {
            Double connectInterval = Double.parseDouble(connectIntervalField.getText().toString());
            if (connectInterval < 0) {
                if (errorText.length() > 0) {
                    errorText += "\n";
                }
                errorText += getResources().getString(R.string.valid_number);
            }
        }
        catch (NumberFormatException e) { // occurs if no value is entered
            if (errorText.length() > 0) {
                errorText += "\n";
            }
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

}
