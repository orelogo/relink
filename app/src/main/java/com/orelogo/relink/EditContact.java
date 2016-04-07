package com.orelogo.relink;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class EditContact extends AppCompatActivity {

    private Spinner timeScaleSpinner; // spinner for selecting time scale

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        displayContactData();
    }

    private void displayContactData() {
        Intent intent = getIntent();

        int rowId = intent.getIntExtra(MainActivity.ROW_ID, -1);
        String name = intent.getStringExtra(MainActivity.NAME);
        long lastConnect = intent.getLongExtra(MainActivity.LAST_CONNECT, -1);
        long nextConnect = intent.getLongExtra(MainActivity.NEXT_CONNECT, -1);
        double connectInterval = intent.getDoubleExtra(MainActivity.CONNECT_INTERVAL, -1);
        char timeScale = intent.getCharExtra(MainActivity.TIME_SCALE, 'x');

        EditText nameField = (EditText) findViewById(R.id.name);
        nameField.setText(name);

        EditText connectIntervalField = (EditText) findViewById(R.id.connect_interval);
        connectIntervalField.setText(Double.toString(connectInterval));

        loadSpinner(timeScale);
    }

    /**
     * Load spinner with selection based on timeScale char.
     */
    private void loadSpinner(char timeScale) {
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

}
