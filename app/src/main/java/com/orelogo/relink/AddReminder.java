package com.orelogo.relink;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
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

        // enable up button in app bar
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

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
     * Load spinner for selecting time scale for input (days, weeks, moths, years) based on
     * settings.
     */
    private void loadSpinner() {
        timeScaleSpinner = (Spinner) findViewById(R.id.time_scale_spinner);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String timeScale = preferences.getString(
                SettingsFragment.DEFAULT_TIME_SCALE, getResources().getString(R.string.week_default));

        loadTimeScaleSpinner(this, timeScaleSpinner, timeScale);
    }


    /**
     * Load time scale spinner with corresponding time scale.
     *
     * @param context context where spinner is located
     * @param spinner the spinner
     * @param timeScale time scale (d, w, m, or y)
     */
    static void loadTimeScaleSpinner(Context context, Spinner spinner, String timeScale) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context, R.array.time_scale_plural, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int spinnerSelection = Convert.getSpinnerSelection(timeScale);
        spinner.setSelection(spinnerSelection);
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
     * Add a new reminder entry into the database.
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
            String spinnerValue = timeScaleSpinner.getSelectedItem().toString();
            // time scale to be used with interval value
            String timeScale = Convert.getTimeScaleChar(spinnerValue);

            long currentTime = System.currentTimeMillis();
            long nextConnect = Convert.getNextConnect(connectInterval, timeScale);

            // add name and next connect time to database
            DBAdapter db = new DBAdapter(this); // database adapter for interacting with database
            db.open();
            db.insertRow(name, currentTime, nextConnect, connectInterval, timeScale);
            db.close();
            finish(); // finish activity
        }
    }

    /**
     * Determines if user input is valid. If input is invalid, an error message is displayed.
     *
     * @return true if user supplied valid input, otherwise, false
     */
    private boolean isInputValid() {

        // field to display error message
        TextView errorField = (TextView) findViewById(R.id.error);
        String errorText = ""; // error message

        // check if user supplied a name
        EditText nameField = (EditText) findViewById(R.id.name);
        errorText += errorName(this, nameField);

        // check if user supplied a valid number, 0 considered valid
        EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
        if (errorText.length() > 0) {
            errorText += "\n";
        }
        errorText += errorTimeValue(this, connectIntervalField);


        if (errorText.length() == 0) {
            return true;
        }
        else {
            errorField.setText(errorText); // display error message
            return false;
        }
    }

    /**
     * Checks if the EditText field is a valid name, not empty.
     *
     * @param context context
     * @param nameField field with name
     * @return error message, empty string if no error
     */
    static String errorName(Context context, EditText nameField) {
        String errorText = ""; // error message

        String name = nameField.getText().toString();
        if (name.length() == 0 ) {
            errorText += context.getResources().getString(R.string.valid_name);
        }

        return errorText;
    }

    /**
     * Checks if EditText field is a valid number, > 0.
     *
     * @param context context
     * @param connectIntervalField field with number value
     * @return error message, empty string if no error
     */
    static String errorTimeValue(Context context, EditText connectIntervalField) {

        String errorText = ""; // error message

        try {
            Double connectInterval = Double.parseDouble(connectIntervalField.getText().toString());
            if (connectInterval < 0) {
                errorText += context.getResources().getString(R.string.valid_number);
            }
        }
        catch (NumberFormatException e) { // occurs if no value is entered
            errorText += context.getResources().getString(R.string.valid_number);
        }

        return errorText;
    }

}
