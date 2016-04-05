package com.orelogo.relink;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Activity for adding a new contact to the app.
 */
public class AddContact extends AppCompatActivity {

    private Spinner timeScaleSpinner; // spinner for selecting time scale
    private final int PICK_CONTACT_REQUEST = 1;  // request code for picking a contact via Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                this, R.array.time_scale_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeScaleSpinner.setAdapter(adapter);
        timeScaleSpinner.setSelection(2);    // set selection to "Months"
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
     * Add contact information to database.
     *
     * @param view view that was clicked
     */
    public void addContact(View view) {

        DBAdapter db = new DBAdapter(this); // database adapter for interacting with database
        db.open();

        // get name from user
        EditText nameField = (EditText) findViewById(R.id.name);
        String name = nameField.getText().toString();

        // get connect time interval from user
        EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
        Double connectIntervalMonths = Double.parseDouble(
                connectIntervalField.getText().toString());

        // get time in milliseconds based on user selected time scale spinner
        long time_ms; // time in milliseconds
        String timeScale = timeScaleSpinner.getSelectedItem().toString();

        switch (timeScale) {
            case "Days":
                time_ms = MainActivity.DAY_MS;
                break;
            case "Weeks":
                time_ms = MainActivity.WEEK_MS;
                break;
            case "Months":
                time_ms = MainActivity.MONTH_MS;
                break;
            case "Years":
                time_ms = MainActivity.YEAR_MS;
                break;
            default: // an error occurred
                time_ms = 0;
                break;
        }

        // calculate time to next connect
        long currentTime =  System.currentTimeMillis();
        long intervalTime = (long) Math.floor(connectIntervalMonths * time_ms);
        long nextConnect = currentTime + intervalTime; // time for next connect (in milliseconds)

        // add name and next connect time to database
        db.insertRow(name, currentTime, nextConnect);

        TextView databaseView = (TextView) findViewById(R.id.database_view);
        databaseView.setText("Contact added!");

        db.close();
        finish(); // finish activity
    }
}
